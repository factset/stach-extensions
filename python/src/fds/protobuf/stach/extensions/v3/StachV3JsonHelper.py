import json
import os
import pandas as pd
from IPython.core.display import HTML
from fds.protobuf.stach.extensions.v3.TableSchema import (TableSchema)

from fds.protobuf.stach.extensions.v3.StachV3SchemaProperties import (
    StachV3SchemaProperties as StachV3SchemaProperties
)

from fds.protobuf.stach.extensions.v3.StachV3Constants import (
    ignored_columns,
    group_prefix,
    level_name_column,
    security_name_column,
    column_separator
)

def get_stach_v3_properties(source, property, sub_property=None):
    if property in source:
        token = source[property]
        if sub_property:
            return token.get(sub_property, "")
        return token
    return ""

def get_levels(rows):
    levels = set()
    for row in rows:
        level = row.get("level")
        if level is not None:
            try:
                levels.add(int(level))
            except ValueError:
                continue
    return sorted(levels)

def _load_json(jsonFilePath):
    """Load and parse JSON file. Returns parsed dict or None if file doesn't exist."""
    if not os.path.exists(jsonFilePath):
        return None

    with open(jsonFilePath, "r") as f:
        fileContent = f.read()
    return json.loads(fileContent)

def _parse_table(stachJObject):
    """Parse table section from JSON object."""
    stachTable = TableSchema()
    stachV3TableJson = get_stach_v3_properties(stachJObject, StachV3SchemaProperties.Table)
    if stachV3TableJson:
        stachTable.SplitResult = stachV3TableJson.get(StachV3SchemaProperties.SplitResult, {})
    return stachTable

def _parse_views(stachJObject):
    """Parse views section from JSON object."""
    customStachV3Views = TableSchema()
    stachV3ViewsJson = get_stach_v3_properties(stachJObject, StachV3SchemaProperties.Views)
    if stachV3ViewsJson:
        viewsArray = stachV3ViewsJson
        if viewsArray:
            stachV3ViewObject = viewsArray[0]
            customStachV3Views.ViewName = get_stach_v3_properties(stachV3ViewObject, StachV3SchemaProperties.Name)
            viewColumns = get_stach_v3_properties(
                get_stach_v3_properties(stachV3ViewObject, StachV3SchemaProperties.Table),
                StachV3SchemaProperties.Columns
            )
            if viewColumns:
                customStachV3Views.ViewColumns = viewColumns
            viewHeaderColumns = get_stach_v3_properties(
                get_stach_v3_properties(stachV3ViewObject, StachV3SchemaProperties.Table),
                StachV3SchemaProperties.Headers
            )
            if viewHeaderColumns:
                customStachV3Views.ViewHeaders = viewHeaderColumns
    return customStachV3Views

def _parse_data_table(stachJObject):
    """Parse columns and rows from JSON object."""
    customStachV3Table = TableSchema()
    stachV3ColumnsJson = get_stach_v3_properties(stachJObject, StachV3SchemaProperties.Columns)
    if stachV3ColumnsJson:
        customStachV3Table.Columns = stachV3ColumnsJson

    stachV3RowsJson = get_stach_v3_properties(stachJObject, StachV3SchemaProperties.Rows)
    if stachV3RowsJson:
        customStachV3Table.Rows = stachV3RowsJson

    return customStachV3Table

def _parse_multi_level_headers(stachJObject):
    """Parse multi-level headers table from JSON object."""
    multiLevelHeaders = TableSchema()
    stachV3MlhtJson = get_stach_v3_properties(stachJObject, StachV3SchemaProperties.MultiLevelHeadersTable)
    if stachV3MlhtJson:
        mlhtObject = stachV3MlhtJson
        mlhtColumns = get_stach_v3_properties(mlhtObject, StachV3SchemaProperties.Columns)
        if mlhtColumns:
            multiLevelHeaders.Columns = mlhtColumns
        mlhtRows = get_stach_v3_properties(mlhtObject, StachV3SchemaProperties.Rows)
        if mlhtRows:
            multiLevelHeaders.Rows = mlhtRows
    return multiLevelHeaders

def _build_column_headers_mapping(stachTable, customStachV3Table, customStachV3Views, multiLevelHeaders):
    """Build column header mapping including level columns and multi-level headers."""
    columnHeadersMapping = {}
    concatColumnsList = {}

    # Add group_prefix level columns based on row levels
    levels = get_levels(customStachV3Table.Rows)
    if levels:
        maxLevel = max(levels)
        for i in range(maxLevel + 1):
            levelColumnName = f"{group_prefix}{i}"
            if levelColumnName not in columnHeadersMapping:
                columnHeadersMapping[levelColumnName] = levelColumnName

    # Process multi-level headers and concatenate columns
    for rowIndex, row in enumerate(multiLevelHeaders.Rows or []):
        concatColumnValue = ""
        primaryKeyColumn = ""
        for colIndex, col in enumerate(multiLevelHeaders.Columns or []):
            columnObject = str(col.get(StachV3SchemaProperties.Name, ""))
            if columnObject:
                headerColumnValue = row.get(columnObject, "")
                mlhtReferenceName = str(stachTable.SplitResult.get(StachV3SchemaProperties.MultiLevelHeadersTableReference, "")) if stachTable.SplitResult else ""
                if columnObject == mlhtReferenceName:
                    primaryKeyColumn = headerColumnValue
                if headerColumnValue:
                    if customStachV3Views.ViewHeaders and headerColumnValue in customStachV3Views.ViewHeaders:
                        headerColumnValue = customStachV3Views.ViewHeaders.get(headerColumnValue, headerColumnValue)
                    concatColumnValue = headerColumnValue + column_separator + concatColumnValue
        if primaryKeyColumn:
            concatColumnsList[primaryKeyColumn] = concatColumnValue.rstrip(column_separator)

    # Map column names against view headers and concat list
    for col in customStachV3Table.Columns or []:
        columnObject = str(col.get(StachV3SchemaProperties.Name, ""))
        if columnObject:
            headerName = columnObject
            if concatColumnsList and columnObject in concatColumnsList:
                headerName = concatColumnsList[columnObject]
            elif customStachV3Views.ViewHeaders and columnObject in customStachV3Views.ViewHeaders:
                headerName = customStachV3Views.ViewHeaders.get(columnObject, columnObject)
            columnHeadersMapping[columnObject] = headerName

    return columnHeadersMapping

def _build_data_table(customStachV3Table, columnHeadersMapping):
    """Create and populate DataFrame with data rows."""
    dataTable = pd.DataFrame(columns=columnHeadersMapping.values())

    # Loop through each row and populate data
    for rowDict in customStachV3Table.Rows or []:
        dataRow = {}
        securityColumn = next((k for k, v in columnHeadersMapping.items() if security_name_column.lower() in v.lower()), None)
        securityNameColumnData = rowDict.get(securityColumn, "") if securityColumn else ""
        levelColumn = next((k for k in columnHeadersMapping if level_name_column in k), None)
        levelIndex = int(rowDict.get(levelColumn, 0)) if levelColumn else 0

        for columnKey, columnName in columnHeadersMapping.items():
            if group_prefix not in columnKey:
                columnData = rowDict.get(columnKey)
                if columnData is not None and columnName:
                    dataRow[columnName] = columnData
            else:
                groupIndex = int(columnKey.replace(group_prefix, ""))
                if levelIndex == groupIndex:
                    dataRow[columnName] = securityNameColumnData or ""
                elif dataTable.shape[0] > 0 and levelIndex > 0 and groupIndex < levelIndex:
                    dataRow[columnName] = dataTable.iloc[-1][columnName]
        dataTable = pd.concat([dataTable, pd.DataFrame([dataRow])], ignore_index=True)

    # Remove ignored columns
    for column in ignored_columns:
        if column in dataTable.columns:
            dataTable.drop(column, axis=1, inplace=True)

    return dataTable