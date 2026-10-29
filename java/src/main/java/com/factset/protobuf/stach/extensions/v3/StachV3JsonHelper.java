package com.factset.protobuf.stach.extensions.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StachV3JsonHelper extends StachV3Constants {

    // JSON-specific fields
    public LinkedHashMap<String, String> columnHeadersMapping = new LinkedHashMap<>();
    public List<Map<String, String>> dataTable = new ArrayList<>();
    public LinkedHashMap<String, String> concatColumnsList = new LinkedHashMap<>();
    public List<List<Map<String, String>>> dataSet = new ArrayList<>();
    public TableSchema jsonStachTable = new TableSchema();
    public TableSchema customStachV3Table = new TableSchema();
    public TableSchema customStachV3Views = new TableSchema();
    public TableSchema jsonMultiLevelHeaders = new TableSchema();

    // Parses the table metadata (SplitResult) from the stach JSON object
    public void parseTableMetadata(JSONObject stachJObject) throws Exception {
        String stachV3TableJson = getStachV3Properties(stachJObject, StachV3SchemaProperties.Table);
        if (!stachV3TableJson.isEmpty()) {
            JSONObject tableSchema = new JSONObject(stachV3TableJson);
            String stachV3SplitResultJson = getStachV3Properties(tableSchema, StachV3SchemaProperties.SplitResult);
            if (!stachV3SplitResultJson.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                jsonStachTable.SplitResult = mapper.readValue(
                        tableSchema.get(StachV3SchemaProperties.SplitResult).toString(),
                        Map.class
                );
            }
        }
    }

    // Parses the views section (view name, columns, headers) from the stach JSON object
    public void parseViews(JSONObject stachJObject) throws Exception {
        String stachV3ViewsJson = getStachV3Properties(stachJObject, StachV3SchemaProperties.Views);
        if (stachV3ViewsJson.isEmpty()) {
            return;
        }

        JSONArray viewsArray = new JSONArray(stachV3ViewsJson);
        if (viewsArray.length() == 0) {
            return;
        }

        JSONObject stachV3ViewObject = new JSONObject(viewsArray.get(0).toString());
        ObjectMapper mapper = new ObjectMapper();

        customStachV3Views.ViewName = getStachV3Properties(stachV3ViewObject, StachV3SchemaProperties.Name);

        String viewColumns = getStachV3Properties(stachV3ViewObject, StachV3SchemaProperties.Table, StachV3SchemaProperties.Columns);
        if (!viewColumns.isEmpty()) {
            customStachV3Views.ViewColumns = mapper.readValue(viewColumns, List.class);
        }

        String viewHeaderColumns = getStachV3Properties(stachV3ViewObject, StachV3SchemaProperties.Table, StachV3SchemaProperties.Headers);
        if (!viewHeaderColumns.isEmpty()) {
            customStachV3Views.ViewHeaders = mapper.readValue(viewHeaderColumns, Map.class);
        }
    }

    // Parses columns and rows from the stach JSON object; also populates group-level columns
    public void parseColumnsAndRows(JSONObject stachJObject) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String stachV3ColumnsJson = getStachV3Properties(stachJObject, StachV3SchemaProperties.Columns);
        if (!stachV3ColumnsJson.isEmpty()) {
            customStachV3Table.Columns = mapper.readValue(stachV3ColumnsJson, List.class);
        }

        String stachV3RowsJson = getStachV3Properties(stachJObject, StachV3SchemaProperties.Rows);
        if (!stachV3RowsJson.isEmpty()) {
            List<Map<String, Object>> rawRows = mapper.readValue(stachV3RowsJson, new TypeReference<List<Map<String, Object>>>() {});
            customStachV3Table.Rows = rawRows.stream()
                    .map(row -> {
                        Map<String, String> strRow = new LinkedHashMap<>();
                        row.forEach((k, v) -> strRow.put(k, v != null ? v.toString() : ""));
                        return strRow;
                    })
                    .collect(Collectors.toList());
        }

        long[] levels = getLevels(customStachV3Table.Rows);
        if (levels != null && levels.length > 0) {
            long maxLevel = Arrays.stream(levels).max().getAsLong();
            for (int i = 0; i <= maxLevel; i++) {
                String levelColumnName = groupPrefix + i;
                columnHeadersMapping.putIfAbsent(levelColumnName, levelColumnName);
            }
        }
    }

    // Parses the multi-level headers table (columns and rows) from the stach JSON object
    public void parseMultiLevelHeaders(JSONObject stachJObject) throws Exception {
        String stachV3MlhtJson = getStachV3Properties(stachJObject, StachV3SchemaProperties.MultiLevelHeadersTable);
        if (stachV3MlhtJson.isEmpty()) {
            return;
        }

        JSONObject mlhtObject = new JSONObject(stachV3MlhtJson);
        ObjectMapper mapper = new ObjectMapper();

        String mlhtColumns = getStachV3Properties(mlhtObject, StachV3SchemaProperties.Columns);
        if (!mlhtColumns.isEmpty()) {
            jsonMultiLevelHeaders.Columns = mapper.readValue(mlhtColumns, List.class);
        }

        String mlhtRows = getStachV3Properties(mlhtObject, StachV3SchemaProperties.Rows);
        if (!mlhtRows.isEmpty()) {
            jsonMultiLevelHeaders.Rows = mapper.readValue(mlhtRows, List.class);
        }
    }

    // Builds concatColumnsList by iterating multi-level header rows and concatenating header values
    public void buildConcatColumnsList() {
        if (jsonMultiLevelHeaders.Rows == null || jsonMultiLevelHeaders.Columns == null) {
            return;
        }

        for (int rowIndex = 0; rowIndex < jsonMultiLevelHeaders.Rows.size(); rowIndex++) {
            String concatColumnValue = "";
            String primaryKeyColumn = "";

            for (int colIndex = 0; colIndex < jsonMultiLevelHeaders.Columns.size(); colIndex++) {
                Map<String, Object> columnObj = jsonMultiLevelHeaders.Columns.get(colIndex);
                String columnObject = columnObj.get(StachV3SchemaProperties.Name).toString();
                if (columnObject == null) {
                    continue;
                }

                Map<String, String> rowObj = jsonMultiLevelHeaders.Rows.get(rowIndex);
                String headerColumnValue = rowObj.get(columnObject);

                String mlhtReferenceName = jsonStachTable.SplitResult != null
                        ? jsonStachTable.SplitResult.getOrDefault(StachV3SchemaProperties.MultiLevelHeadersTableReference, "").toString()
                        : "";
                if (columnObject.equals(mlhtReferenceName)) {
                    primaryKeyColumn = headerColumnValue;
                }

                if (headerColumnValue != null && !headerColumnValue.isEmpty()) {
                    if (customStachV3Views != null && customStachV3Views.ViewHeaders != null
                            && customStachV3Views.ViewHeaders.containsKey(headerColumnValue)) {
                        headerColumnValue = customStachV3Views.ViewHeaders.get(headerColumnValue);
                    }
                    concatColumnValue = headerColumnValue + columnSeparator + concatColumnValue;
                }
            }

            if (primaryKeyColumn != null && !primaryKeyColumn.isEmpty()) {
                concatColumnsList.put(primaryKeyColumn, concatColumnValue.replaceAll("\\s*\\|\\s*$", ""));
            }
        }
    }

    // Builds columnHeadersMapping by resolving each column name against view headers or concatColumnsList
    public void buildColumnHeadersMapping() {
        if (customStachV3Table.Columns == null) {
            return;
        }

        for (Map<String, Object> colObj : customStachV3Table.Columns) {
            if (!colObj.containsKey(StachV3SchemaProperties.Name)) {
                continue;
            }
            String columnObject = colObj.get(StachV3SchemaProperties.Name).toString();
            if (columnObject == null || columnObject.isEmpty()) {
                continue;
            }

            String headerName = columnObject;
            if (!concatColumnsList.isEmpty() && concatColumnsList.containsKey(columnObject)) {
                headerName = concatColumnsList.get(columnObject);
            } else if (customStachV3Views != null && customStachV3Views.ViewHeaders != null
                    && customStachV3Views.ViewHeaders.containsKey(columnObject)) {
                headerName = customStachV3Views.ViewHeaders.get(columnObject);
            }
            columnHeadersMapping.put(columnObject, headerName);
        }
    }

    // Iterates data rows and populates dataTable, including group hierarchy columns
    public void buildDataTable() {
        if (customStachV3Table.Rows == null) {
            return;
        }

        for (int rowIndex = 0; rowIndex < customStachV3Table.Rows.size(); rowIndex++) {
            Map<String, String> rowDict = customStachV3Table.Rows.get(rowIndex);
            Map<String, String> dataRow = new java.util.HashMap<>();

            String securityColumnKey = columnHeadersMapping.entrySet().stream()
                    .filter(x -> x.getValue().toLowerCase().contains(securityNameColumn.toLowerCase()))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse("");
            String securityNameColumnData = (!securityColumnKey.isEmpty() && rowDict.containsKey(securityColumnKey))
                    ? rowDict.getOrDefault(securityColumnKey, "")
                    : "";

            String levelColumnKey = columnHeadersMapping.entrySet().stream()
                    .filter(x -> x.getKey().toLowerCase().contains(levelNameColumn.toLowerCase()))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse("");
            int levelIndex = 0;
            if (!levelColumnKey.isEmpty() && rowDict.containsKey(levelColumnKey)) {
                try {
                    levelIndex = Integer.parseInt(rowDict.get(levelColumnKey));
                } catch (Exception e) {
                    levelIndex = 0;
                }
            }

            populateDataRow(dataRow, rowDict, rowIndex, levelIndex, securityNameColumnData);
            dataTable.add(dataRow);
        }
    }

    // Fills a single data row with regular column values and group hierarchy columns
    protected void populateDataRow(Map<String, String> dataRow, Map<String, String> rowDict,
                                   int rowIndex, int levelIndex, String securityNameColumnData) {
        for (Map.Entry<String, String> column : columnHeadersMapping.entrySet()) {
            if (!column.getKey().contains(groupPrefix)) {
                String columnData = rowDict.getOrDefault(column.getKey(), "");
                String columnName = column.getValue();
                if (columnName == null || columnName.isEmpty()) {
                    continue;
                }
                dataRow.put(columnName, columnData);
            } else {
                String columnName = column.getKey();
                int groupIndex = 0;
                try {
                    groupIndex = Integer.parseInt(columnName.replace(groupPrefix, ""));
                } catch (Exception e) {
                    groupIndex = 0;
                }

                if (levelIndex == groupIndex) {
                    dataRow.put(columnName, securityNameColumnData);
                } else if (rowIndex > 0 && levelIndex > 0 && groupIndex < levelIndex) {
                    dataRow.put(columnName, dataTable.get(rowIndex - 1).get(columnName));
                }
            }
        }
    }

    // Helper to get levels
    protected static long[] getLevels(List<Map<String, String>> rows) {
        List<Long> levels = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return new long[0];
        }
        for (Map<String, String> row : rows) {
            if (!row.containsKey("level")) {
                continue;
            }
            String columnX = row.get("level");
            if (columnX != null && !columnX.isEmpty()) {
                try {
                    long level = Long.parseLong(columnX);
                    if (!levels.contains(level)) {
                        levels.add(level);
                    }
                } catch (Exception e) {
                    // ignore parse error
                }
            }
        }
        return levels.stream().mapToLong(Long::longValue).toArray();
    }

    // Helper to get stach v3 json properties
    protected static String getStachV3Properties(JSONObject source, String property) throws JSONException {
        if (source.has(property) && source.get(property) != null) {
            return source.get(property).toString();
        }
        return "";
    }

    // Helper to get stach v3 json sub-properties
    protected static String getStachV3Properties(JSONObject source, String property, String subProperty) throws JSONException {
        if (source.has(property) && source.get(property) != null) {
            JSONObject token = source.getJSONObject(property);
            if (token.has(subProperty) && token.get(subProperty) != null) {
                return token.get(subProperty).toString();
            }
        }
        return "";
    }
}