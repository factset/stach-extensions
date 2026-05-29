import pyarrow as pa
import pyarrow.ipc as ipc
import base64
import pandas as pd
import json
from google.protobuf import json_format
from IPython.core.display import HTML
import io
from fds.protobuf.stach.extensions.v3.StachV3ArrowHelper import (
    _read_arrow_stream,
    _setup_level_columns,
    _parse_table_metadata as _parse_table_metadata_arrow,
    _parse_views_metadata as _parse_views_metadata_arrow,
    _parse_multi_level_headers as _parse_multi_level_headers_arrow,
    _build_column_headers_mapping as _build_column_headers_mapping_arrow,
    _populate_dataframe,
    _remove_ignored_columns,
    get_arrow_stream
)
from fds.protobuf.stach.extensions.v3.StachV3JsonHelper import (
    _parse_table,
    _parse_views,
    _parse_data_table,
    _parse_multi_level_headers,
    _build_column_headers_mapping,
    _build_data_table
)
from fds.protobuf.stach.extensions.v3.StachV3Constants import (
    group_prefix,
    level_name_column,
    security_name_column,
    column_separator,
    ignored_columns
)

# Module-level configuration variables (set before calling conversion functions)
column_headers_mapping = {}

def ConvertArrowFileToTable(file_path):
    global column_headers_mapping
    column_headers_mapping = {}  # Reset for each conversion
    data_set = {}

    # Read arrow file
    arrow_bytes, reader = _read_arrow_stream(file_path)
    schema = reader.schema

    # Setup level columns
    _setup_level_columns(arrow_bytes, column_headers_mapping)

    # Parse metadata
    view_headers = {}
    concat_columns_list = {}
    stach_table = None

    if schema.metadata is not None:
        metadata = {k.decode(): v.decode() for k, v in schema.metadata.items()}
        stach_table = _parse_table_metadata_arrow(metadata)
        view_headers = _parse_views_metadata_arrow(metadata)
        concat_columns_list = _parse_multi_level_headers_arrow(schema.metadata, stach_table, view_headers)

    # Build column headers mapping
    _build_column_headers_mapping_arrow(schema, view_headers, concat_columns_list, column_headers_mapping)

    # Populate DataFrame
    data_table = _populate_dataframe(reader, column_headers_mapping)

    # Remove ignored columns
    data_table = _remove_ignored_columns(data_table, ignored_columns)

    data_set['table'] = data_table

    return data_set

def ConvertArrowStreamToTable(arrow_bytes):
    global column_headers_mapping
    column_headers_mapping = {}  # Reset for each conversion
    data_set = {}

    # Get arrow stream from bytes
    reader = get_arrow_stream(arrow_bytes)
    if reader is None:
        raise Exception("Failed to create ArrowStreamReader from arrow_bytes")

    schema = reader.schema

    # Setup level columns
    _setup_level_columns(arrow_bytes, column_headers_mapping)

    # Parse metadata
    view_headers = {}
    concat_columns_list = {}
    stach_table = None

    if schema.metadata is not None:
        metadata = {k.decode(): v.decode() for k, v in schema.metadata.items()}
        stach_table = _parse_table_metadata_arrow(metadata)
        view_headers = _parse_views_metadata_arrow(metadata)
        concat_columns_list = _parse_multi_level_headers_arrow(schema.metadata, stach_table, view_headers)

    # Build column headers mapping
    _build_column_headers_mapping_arrow(schema, view_headers, concat_columns_list, column_headers_mapping)

    # Populate DataFrame
    data_table = _populate_dataframe(reader, column_headers_mapping)

    # Remove ignored columns
    data_table = _remove_ignored_columns(data_table, ignored_columns)

    data_set['table'] = data_table

    return data_set

def ConvertJsonToTable(jsonFileContent):
    global column_headers_mapping
    column_headers_mapping = {}  # Reset for each conversion
    data_set = {}

    if jsonFileContent is None:
        return
    stachJObject = json.loads(jsonFileContent)
    if stachJObject is None:
        return

    stachTable = _parse_table(stachJObject)
    customStachV3Views = _parse_views(stachJObject)
    customStachV3Table = _parse_data_table(stachJObject)
    multiLevelHeaders = _parse_multi_level_headers(stachJObject)
    columnHeadersMapping = _build_column_headers_mapping(stachTable, customStachV3Table, customStachV3Views, multiLevelHeaders)
    data_table = _build_data_table(customStachV3Table, columnHeadersMapping)

    data_set['table'] = data_table
    return data_set