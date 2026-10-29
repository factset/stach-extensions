import pyarrow as pa
import pyarrow.ipc as ipc
import base64
import pandas as pd
import json
from google.protobuf import json_format
import io

from fds.protobuf.stach.extensions.v3.StachV3Constants import (
    ignored_columns,
    group_prefix,
    level_name_column,
    security_name_column,
    column_separator
)

try:
    from fds.protobuf.stach.table import Table_pb2
    from fds.protobuf.stach.v3 import Views_pb2
except ImportError as e:
    # Handle case where protobuf messages are not available
    Table_pb2 = None
    Views_pb2 = None

def get_data_row(record_batch, row_index, data_table, column_headers_mapping):
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
                        data_row[column_name] = data_table.iloc[row_index - 1][column_name]
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

def _parse_views_metadata(metadata):
    """Parse and return view headers from metadata."""
    view_headers = {}
    if Views_pb2 is None:
        return view_headers
    if "fds:stach:views" in metadata:
        try:
            view_object = metadata["fds:stach:views"]
            views_array_decoded = base64.b64decode(view_object)
            views = Views_pb2.Views()
            views.ParseFromString(views_array_decoded)
            if views.views:
                first_view = views.views[0]
                if first_view.table.headers:
                    view_headers = dict(first_view.table.headers)
        except Exception as e:
            # Handle parsing errors gracefully - continue without view headers
            pass
    return view_headers

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
                        for row_index in range(multi_level_headers.num_rows):
                            concat_column_value = ''
                            primary_key_column = ''

                            for col_index in range(multi_level_headers.num_columns):
                                column_object = multi_level_headers.column(col_index)
                                column_name = multi_level_headers.column_names[col_index]
                                header_column_value = get_column_value(column_object, row_index)

                                if column_name == getattr(stach_table.split_result, "multi_level_headers_table_reference", ""):
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

def _build_column_headers_mapping(schema, view_headers, concat_columns_list, column_headers_mapping):
    """Build column headers mapping from schema field names."""
    for field in schema:
        header_name = field.name
        if concat_columns_list:
            header_name = concat_columns_list.get(field.name, field.name)
        else:
            header_name = view_headers.get(field.name, field.name)
        column_headers_mapping[field.name] = header_name

def _populate_dataframe(reader, column_headers_mapping):
    """Create and populate DataFrame from record batches."""
    columns = [column_headers_mapping[k] for k in column_headers_mapping]
    data_table = pd.DataFrame(columns=columns)

    if hasattr(reader, "num_record_batches"):
        for idx in range(reader.num_record_batches):
            batch = reader.get_batch(idx)
            for row_index in range(batch.num_rows):
                data_row = get_data_row(batch, row_index, data_table, column_headers_mapping)
                data_table = pd.concat([data_table, pd.DataFrame([data_row])], ignore_index=True)
    else:
        for record_batch in reader:
            for row_index in range(record_batch.num_rows):
                data_row = get_data_row(record_batch, row_index, data_table, column_headers_mapping)
                data_table = pd.concat([data_table, pd.DataFrame([data_row])], ignore_index=True)

    return data_table

def _remove_ignored_columns(data_table, ignored_columns):
    """Remove ignored columns from DataFrame."""
    for column in ignored_columns:
        if column in data_table.columns:
            data_table.drop(column, axis=1, inplace=True)
    return data_table

