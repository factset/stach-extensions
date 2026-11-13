import pyarrow as pa
import pyarrow.ipc as ipc
import base64
import pandas as pd
import json
from google.protobuf import json_format
import io
import logging

from fds.protobuf.stach.extensions.v3.StachV3Constants import (
    ignored_columns,
    group_prefix,
    level_name_column,
    security_name_column,
    column_separator
)

logger = logging.getLogger(__name__)

# Each import is guarded separately so a missing module does not disable the others.
try:
    from fds.protobuf.stach.table import Table_pb2
except ImportError as e:
    logger.warning("Table protobuf not available; table metadata will be skipped: %s", e)
    Table_pb2 = None

try:
    from fds.protobuf.stach.v3 import Views_pb2
except ImportError as e:
    logger.warning("Views protobuf not available; view headers will be skipped: %s", e)
    Views_pb2 = None

def get_data_row(record_batch, row_index, rows, column_headers_mapping):
    """Build one row dict. `rows` holds the rows already built, so the previous row can be read from it."""
    data_row = {}
    security_column = next((k for k, v in column_headers_mapping.items() if security_name_column in v.lower()), None)
    security_name_column_data = record_batch.column(security_column) if security_column else None
    level_column = next((k for k, v in column_headers_mapping.items() if level_name_column in k.lower()), None)
    level_column_data = record_batch.column(level_column) if level_column else None
    level_index = int(get_column_value(level_column_data, row_index)) if level_column_data else 0

    for column in column_headers_mapping:
        if group_prefix not in column:
            column_data = record_batch.column(column) if column else None
            column_name = column_headers_mapping[column]
            if column_data is not None and column_name:
                data_row[column_name] = get_column_value(column_data, row_index)
        else:
            column_name = column
            group_index = int(column_name.replace(group_prefix, ""))
            if level_index == group_index:
                if security_name_column_data is not None:
                    data_row[column_name] = get_column_value(security_name_column_data, row_index)
                else:
                    data_row[column_name] = ''
            else:
                if row_index > 0 and level_index > 0 and group_index < level_index:
                    if security_name_column_data is not None:
                        data_row[column_name] = rows[-1].get(column_name)
                    else:
                        data_row[column_name] = ''
    return data_row

def is_arrow_file(stream):
    arrow_file_magic = b'ARROW1'
    stream.seek(0)
    buffer = stream.read(6)
    stream.seek(0)
    return buffer == arrow_file_magic

def get_arrow_stream(arrow_bytes):    
    arrow_stream = io.BytesIO(arrow_bytes)
    if is_arrow_file(arrow_stream):
        arrow_stream.seek(0)
        return ipc.RecordBatchFileReader(arrow_stream)
    else:
        arrow_stream.seek(0)
        return ipc.RecordBatchStreamReader(arrow_stream)

def get_levels(arrow_bytes: bytes):
    levels = []
    reader = get_arrow_stream(arrow_bytes)
    if reader is None:
        raise Exception("Failed to create ArrowStreamReader from arrow_bytes.")
    
     # Read first record batch
    try:
       record_batch = reader.read_next_batch()
    except AttributeError:
        # For pa.ipc.RecordBatchFileReader, use get_batch(0)
        if hasattr(reader, "num_record_batches") and reader.num_record_batches > 0:
            record_batch = reader.get_batch(0)
        else:
            record_batch = None

    if record_batch is not None:
         try:
             column_index = record_batch.schema.get_field_index("level")
         except (KeyError, ValueError):
             column_index = -1
         if column_index >= 0:
             column = record_batch.column(column_index)
             levels = [int(x.as_py()) for x in column]
    return levels
    
def get_column_value(column_array, row_index):    
    if column_array is None:
        return ''
    if pa.types.is_int32(column_array.type):
        return str(column_array[row_index].as_py())
    elif pa.types.is_int64(column_array.type):
        return str(column_array[row_index].as_py())
    elif pa.types.is_string(column_array.type):
        return column_array[row_index].as_py()
    elif pa.types.is_boolean(column_array.type):
        return str(column_array[row_index].as_py())
    elif pa.types.is_float64(column_array.type):
        return str(column_array[row_index].as_py())
    else:
        return ''

def read_multi_level_headers_schema(multi_level_headers_records, schema):
    columns = []
    n_columns = len(schema)
    n_batches = len(multi_level_headers_records)
    for i in range(n_columns):
        column_arrays = []
        for j in range(n_batches):
            col_value = multi_level_headers_records[j].column(i)
            column_arrays.append(col_value)
        columns.append(pa.chunked_array(column_arrays))
    return pa.Table.from_arrays(columns, schema.names)

def generate_multi_level_headers(multi_level_headers_array_decoded):
    multi_level_headers_records = []
    mlh_table = None
    try:
        memory_stream = pa.BufferReader(multi_level_headers_array_decoded)
        reader = ipc.RecordBatchFileReader(memory_stream)
        for i in range(reader.num_record_batches):
            rb = reader.get_batch(i)
            multi_level_headers_records.append(rb)
        mlh_table = read_multi_level_headers_schema(multi_level_headers_records, reader.schema)
    except Exception as ex:
        # Try as stream if file fails
        memory_stream = pa.BufferReader(multi_level_headers_array_decoded)
        reader = ipc.RecordBatchStreamReader(memory_stream)
        for rb in reader:
            multi_level_headers_records.append(rb)
        mlh_table = read_multi_level_headers_schema(multi_level_headers_records, reader.schema)
    return mlh_table

def _read_arrow_stream(file_path):
    """Read arrow file and return bytes and stream reader."""
    with open(file_path, "rb") as f:
        arrow_bytes = f.read()

    reader = get_arrow_stream(arrow_bytes)
    if reader is None:
        raise Exception("Failed to create ArrowStreamReader")

    return arrow_bytes, reader

def _setup_level_columns(arrow_bytes, column_headers_mapping):
    """Read levels from arrow data and add group columns to mapping."""
    levels = get_levels(arrow_bytes)
    if levels:
        max_level = max(levels)
        for i in range(max_level + 1):
            level_column_name = f"{group_prefix}{i}"
            if level_column_name not in column_headers_mapping:
                column_headers_mapping[level_column_name] = level_column_name

def _parse_table_metadata(metadata):
    """Parse and return Table protobuf object from metadata."""
    stach_table = None
    if Table_pb2 is None:
        return stach_table
    if "fds:stach:table" in metadata:
        try:
            table_object = metadata["fds:stach:table"]
            table_array_decoded = base64.b64decode(table_object)
            stach_table = Table_pb2.Table()
            stach_table.ParseFromString(table_array_decoded)
        except Exception as e:
            # Handle parsing errors gracefully - continue without table metadata
            pass
    return stach_table

def _read_varint(buf, pos):
    """Read a protobuf varint starting at pos. Returns (value, next_pos)."""
    result = 0
    shift = 0
    while True:
        byte = buf[pos]
        pos += 1
        result |= (byte & 0x7F) << shift
        if not byte & 0x80:
            return result, pos
        shift += 7

def _iter_proto_fields(buf):
    """Yield (field_number, wire_type, value) for each field of a protobuf message.

    Length-delimited values are returned as bytes; varints as int. Fixed-width fields are skipped.
    """
    pos = 0
    end = len(buf)
    while pos < end:
        key, pos = _read_varint(buf, pos)
        field_number, wire_type = key >> 3, key & 0x7
        if wire_type == 0:
            value, pos = _read_varint(buf, pos)
        elif wire_type == 2:
            length, pos = _read_varint(buf, pos)
            value = buf[pos:pos + length]
            pos += length
        elif wire_type == 1:
            value, pos = None, pos + 8
        elif wire_type == 5:
            value, pos = None, pos + 4
        else:
            raise ValueError(f"unsupported protobuf wire type {wire_type}")
        yield field_number, wire_type, value

def _parse_view_headers_from_bytes(views_bytes):
    """Read the first view's table headers map from the raw Views message.

    Views { repeated View views = 1 }, View { ... Table table = 3 },
    Table { ... map<string, string> headers = 3 }, map entry { key = 1, value = 2 }.
    """
    view_bytes = next((v for f, w, v in _iter_proto_fields(views_bytes) if f == 1 and w == 2), None)
    if view_bytes is None:
        return {}
    table_bytes = next((v for f, w, v in _iter_proto_fields(view_bytes) if f == 3 and w == 2), None)
    if table_bytes is None:
        return {}
    headers = {}
    for f, w, entry in _iter_proto_fields(table_bytes):
        if f != 3 or w != 2:
            continue
        key, value = "", ""
        for ef, ew, ev in _iter_proto_fields(entry):
            if ew == 2 and ef == 1:
                key = ev.decode("utf-8")
            elif ew == 2 and ef == 2:
                value = ev.decode("utf-8")
        headers[key] = value
    return headers

def _parse_views_metadata(metadata):
    """Parse and return view headers from metadata.

    Uses the generated Views protobuf when it is installed; otherwise the headers are read
    straight from the raw message, so view names are available without that package.
    """
    view_headers = {}
    if "fds:stach:views" in metadata:
        try:
            views_array_decoded = base64.b64decode(metadata["fds:stach:views"])
            if Views_pb2 is not None:
                views = Views_pb2.Views()
                views.ParseFromString(views_array_decoded)
                if views.views:
                    first_view = views.views[0]
                    if first_view.table.headers:
                        view_headers = dict(first_view.table.headers)
            else:
                view_headers = _parse_view_headers_from_bytes(views_array_decoded)
        except Exception as e:
            # Handle parsing errors gracefully - continue without view headers
            logger.warning("Could not parse views metadata; view headers skipped: %s", e)
    return view_headers

def _get_multi_level_reference(schema_metadata, stach_table, multi_level_headers):
    """Return the name of the multi-level headers table's primary-key column.

    Uses the Table protobuf's splitResult reference when that message provides it. Otherwise
    the reference is searched for in the raw table metadata, using the multi-level header
    column names; the first matching column (the primary key column) is used.
    """
    split_result = getattr(stach_table, "split_result", None) if stach_table is not None else None
    reference = getattr(split_result, "multi_level_headers_table_reference", "") if split_result is not None else ""
    if reference:
        return reference

    table_object = schema_metadata.get(b"fds:stach:table")
    if not table_object:
        return ""
    try:
        raw_table = base64.b64decode(table_object)
    except Exception:
        return ""
    for name in multi_level_headers.column_names:
        if name.encode("utf-8") in raw_table:
            return name
    return ""

def _parse_multi_level_headers(schema_metadata, stach_table, view_headers):
    """Parse multi-level headers table and return concatenated columns list."""
    concat_columns_list = {}
    if b"fds:stach:multiLevelHeadersTable" in schema_metadata:
        try:
            multi_level_headers_table_object = schema_metadata[b"fds:stach:multiLevelHeadersTable"].decode()
            multi_level_headers_array_decoded = base64.b64decode(multi_level_headers_table_object)
            if multi_level_headers_array_decoded:
                try:
                    multi_level_headers = generate_multi_level_headers(multi_level_headers_array_decoded)

                    if multi_level_headers:
                        multi_level_ref = _get_multi_level_reference(schema_metadata, stach_table, multi_level_headers)
                        for row_index in range(multi_level_headers.num_rows):
                            concat_column_value = ''
                            primary_key_column = ''

                            for col_index in range(multi_level_headers.num_columns):
                                column_object = multi_level_headers.column(col_index)
                                column_name = multi_level_headers.column_names[col_index]
                                header_column_value = get_column_value(column_object, row_index)

                                if column_name == multi_level_ref:
                                    primary_key_column = header_column_value
                                if header_column_value:
                                    if view_headers and header_column_value in view_headers:
                                        header_column_value = view_headers.get(header_column_value, header_column_value)
                                    concat_column_value = header_column_value + column_separator + concat_column_value

                            if primary_key_column:
                                concat_columns_list[primary_key_column] = concat_column_value.rstrip(column_separator)
                except Exception as exc:
                    # Handle inner parsing errors gracefully
                    pass
        except Exception as e:
            # Handle outer parsing errors gracefully
            pass

    return concat_columns_list

def _make_unique_header(header_name, used_headers):
    """Return header_name, or header_name with a numeric suffix if it is already used."""
    candidate = header_name
    suffix = 1
    while candidate in used_headers:
        candidate = f"{header_name}.{suffix}"
        suffix += 1
    return candidate

def _build_column_headers_mapping(schema, view_headers, concat_columns_list, column_headers_mapping):
    """Build column headers mapping from schema field names.

    Display headers must be unique because the DataFrame is labelled by them. When two
    fields resolve to the same header, the later one gets a suffix (name.1, name.2, ...).
    """
    used_headers = set(column_headers_mapping.values())
    for field in schema:
        header_name = field.name
        if concat_columns_list:
            header_name = concat_columns_list.get(field.name, field.name)
        else:
            header_name = view_headers.get(field.name, field.name)
        header_name = _make_unique_header(header_name, used_headers)
        used_headers.add(header_name)
        column_headers_mapping[field.name] = header_name

def _populate_dataframe(reader, column_headers_mapping):
    """Create and populate DataFrame from record batches.

    Rows are collected in a list and the DataFrame is built once at the end.
    """
    columns = list(column_headers_mapping.values())
    rows = []

    if hasattr(reader, "num_record_batches"):
        for idx in range(reader.num_record_batches):
            batch = reader.get_batch(idx)
            for row_index in range(batch.num_rows):
                rows.append(get_data_row(batch, row_index, rows, column_headers_mapping))
    else:
        for record_batch in reader:
            for row_index in range(record_batch.num_rows):
                rows.append(get_data_row(record_batch, row_index, rows, column_headers_mapping))

    return pd.DataFrame(rows, columns=columns)

def _remove_ignored_columns(data_table, ignored_columns):
    """Remove ignored columns from DataFrame."""
    for column in ignored_columns:
        if column in data_table.columns:
            data_table.drop(column, axis=1, inplace=True)
    return data_table

