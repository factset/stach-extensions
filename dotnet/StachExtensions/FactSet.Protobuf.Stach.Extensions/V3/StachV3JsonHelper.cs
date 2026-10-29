using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;

namespace FactSet.Protobuf.Stach.Extensions.V3
{
    internal static class StachV3JsonHelper
    {
        public static TableSchema ParseTableSchema(JObject stachJObject)
        {
            var stachTable = new TableSchema();
            var stachV3TableJson = GetStachV3Properties(stachJObject, StachV3SchemaProperties.Table);

            if (!string.IsNullOrEmpty(stachV3TableJson))
            {
                var tableSchema = JObject.Parse(stachV3TableJson);
                if (tableSchema != null)
                {
                    var stachV3SplitResult = GetStachV3Properties(tableSchema, StachV3SchemaProperties.SplitResult);
                    if (!string.IsNullOrEmpty(stachV3SplitResult))
                    {
                        stachTable.SplitResult = JsonConvert.DeserializeObject<Dictionary<string, object>>(stachV3SplitResult);
                    }
                }
            }

            return stachTable;
        }

        public static TableSchema ParseViewsSchema(JObject stachJObject)
        {
            var customStachV3Views = new TableSchema();
            var stachV3ViewsJson = GetStachV3Properties(stachJObject, StachV3SchemaProperties.Views);

            if (!string.IsNullOrEmpty(stachV3ViewsJson))
            {
                var viewsArray = JArray.Parse(stachV3ViewsJson);
                if (viewsArray.Count > 0)
                {
                    var stachV3ViewObject = JObject.Parse(viewsArray[0].ToString());
                    customStachV3Views.ViewName = GetStachV3Properties(stachV3ViewObject, StachV3SchemaProperties.Name);

                    var viewColumns = GetStachV3Properties(stachV3ViewObject, StachV3SchemaProperties.Table, StachV3SchemaProperties.Columns);
                    if (!string.IsNullOrEmpty(viewColumns))
                    {
                        customStachV3Views.ViewColumns = JsonConvert.DeserializeObject<List<string>>(viewColumns);
                    }

                    var viewHeaderColumns = GetStachV3Properties(stachV3ViewObject, StachV3SchemaProperties.Table, StachV3SchemaProperties.Headers);
                    if (!string.IsNullOrEmpty(viewHeaderColumns))
                    {
                        customStachV3Views.ViewHeaders = JsonConvert.DeserializeObject<Dictionary<string, string>>(viewHeaderColumns);
                    }
                }
            }

            return customStachV3Views;
        }

        public static TableSchema ParseColumnsAndRows(JObject stachJObject)
        {
            var customStachV3Table = new TableSchema();

            var stachV3ColumnsJson = GetStachV3Properties(stachJObject, StachV3SchemaProperties.Columns);
            if (!string.IsNullOrEmpty(stachV3ColumnsJson))
            {
                customStachV3Table.Columns = JsonConvert.DeserializeObject<List<Dictionary<string, object>>>(stachV3ColumnsJson);
            }

            var stachV3RowsJson = GetStachV3Properties(stachJObject, StachV3SchemaProperties.Rows);
            if (!string.IsNullOrEmpty(stachV3RowsJson))
            {
                customStachV3Table.Rows = JsonConvert.DeserializeObject<List<Dictionary<string, string>>>(stachV3RowsJson);
            }

            return customStachV3Table;
        }

        public static TableSchema ParseMultiLevelHeaders(JObject stachJObject)
        {
            var multiLevelHeaders = new TableSchema();
            var stachV3MlhtJson = GetStachV3Properties(stachJObject, StachV3SchemaProperties.MultiLevelHeadersTable);

            if (!string.IsNullOrEmpty(stachV3MlhtJson))
            {
                var mlhtObject = JObject.Parse(stachV3MlhtJson);

                var mlhtColumns = GetStachV3Properties(mlhtObject, StachV3SchemaProperties.Columns);
                if (!string.IsNullOrEmpty(mlhtColumns))
                {
                    multiLevelHeaders.Columns = JsonConvert.DeserializeObject<List<Dictionary<string, object>>>(mlhtColumns);
                }

                var mlhtRows = GetStachV3Properties(mlhtObject, StachV3SchemaProperties.Rows);
                if (!string.IsNullOrEmpty(mlhtRows))
                {
                    multiLevelHeaders.Rows = JsonConvert.DeserializeObject<List<Dictionary<string, string>>>(mlhtRows);
                }
            }

            return multiLevelHeaders;
        }

        public static void AddGroupColumns(List<Dictionary<string, string>> rows, Dictionary<string, string> columnHeadersMapping)
        {
            var levels = GetLevels(rows);
            if (levels != null && levels.Any())
            {
                var maxLevel = levels.Max();
                for (int i = 0; i <= maxLevel; i++)
                {
                    var levelColumnName = $"{StachV3Constants.GroupPrefix}{i}";
                    if (!columnHeadersMapping.ContainsKey(levelColumnName))
                    {
                        columnHeadersMapping.Add(levelColumnName, levelColumnName);
                    }
                }
            }
        }

        public static Dictionary<string, string> BuildConcatenatedColumns(
            TableSchema multiLevelHeaders,
            TableSchema stachTable,
            TableSchema customStachV3Views)
        {
            var concatColumnsList = new Dictionary<string, string>();

            if (multiLevelHeaders?.Rows == null || multiLevelHeaders?.Columns == null)
            {
                return concatColumnsList;
            }

            for (int rowIndex = 0; rowIndex < multiLevelHeaders.Rows.Count; rowIndex++)
            {
                var concatColumnValue = string.Empty;
                var primaryKeyColumn = string.Empty;

                for (int colIndex = 0; colIndex < multiLevelHeaders.Columns.Count; colIndex++)
                {
                    var columnObject = multiLevelHeaders.Columns[colIndex][StachV3SchemaProperties.Name]?.ToString();
                    if (string.IsNullOrEmpty(columnObject))
                    {
                        continue;
                    }

                    var headerColumnValue = multiLevelHeaders.Rows[rowIndex].ContainsKey(columnObject)
                        ? multiLevelHeaders.Rows[rowIndex][columnObject]
                        : null;

                    var mlhtReferenceName = stachTable?.SplitResult?.ContainsKey(StachV3SchemaProperties.MultiLevelHeadersTableReference) == true
                        ? stachTable.SplitResult[StachV3SchemaProperties.MultiLevelHeadersTableReference]?.ToString()
                        : string.Empty;

                    if (columnObject == mlhtReferenceName)
                    {
                        primaryKeyColumn = headerColumnValue;
                    }

                    if (!string.IsNullOrEmpty(headerColumnValue))
                    {
                        if (customStachV3Views?.ViewHeaders?.ContainsKey(headerColumnValue) == true)
                        {
                            headerColumnValue = customStachV3Views.ViewHeaders[headerColumnValue] ?? headerColumnValue;
                        }
                        concatColumnValue = headerColumnValue + StachV3Constants.ColumnSeparator + concatColumnValue;
                    }
                }

                if (!string.IsNullOrEmpty(primaryKeyColumn))
                {
                    concatColumnsList[primaryKeyColumn] = concatColumnValue.TrimEnd(StachV3Constants.ColumnSeparator.ToCharArray());
                }
            }

            return concatColumnsList;
        }

        public static void MapColumnHeaders(
            TableSchema customStachV3Table,
            TableSchema customStachV3Views,
            Dictionary<string, string> concatColumnsList,
            Dictionary<string, string> columnHeadersMapping)
        {
            if (customStachV3Table?.Columns == null)
            {
                return;
            }

            for (int i = 0; i < customStachV3Table.Columns.Count; i++)
            {
                if (!customStachV3Table.Columns[i].ContainsKey(StachV3SchemaProperties.Name))
                {
                    continue;
                }

                var columnObject = customStachV3Table.Columns[i][StachV3SchemaProperties.Name]?.ToString();
                if (string.IsNullOrEmpty(columnObject))
                {
                    continue;
                }

                var headerName = columnObject;

                if (concatColumnsList?.ContainsKey(columnObject) == true)
                {
                    headerName = concatColumnsList[columnObject];
                }
                else if (customStachV3Views?.ViewHeaders?.ContainsKey(columnObject) == true)
                {
                    headerName = customStachV3Views.ViewHeaders[columnObject] ?? columnObject;
                }

                columnHeadersMapping[columnObject] = headerName;
            }
        }

        public static void CreateDataTableColumns(DataTable dataTable, Dictionary<string, string> columnHeadersMapping)
        {
            dataTable.Columns.AddRange(
                columnHeadersMapping.Select(kvp => new DataColumn(kvp.Value, typeof(string))).ToArray()
            );
        }

        public static void PopulateDataTableRows(
            DataTable dataTable,
            TableSchema customStachV3Table,
            Dictionary<string, string> columnHeadersMapping)
        {
            if (customStachV3Table?.Rows == null)
            {
                return;
            }

            for (int rowIndex = 0; rowIndex < customStachV3Table.Rows.Count; rowIndex++)
            {
                var dataRow = dataTable.NewRow();
                var rowDict = customStachV3Table.Rows[rowIndex];

                var securityNameColumnData = GetSecurityNameColumnData(rowDict, columnHeadersMapping);
                var levelIndex = GetLevelIndex(rowDict, columnHeadersMapping);

                foreach (var column in columnHeadersMapping)
                {
                    if (column.Key.StartsWith(StachV3Constants.GroupPrefix))
                    {
                        PopulateGroupColumn(dataTable, dataRow, column.Key, levelIndex, rowIndex, securityNameColumnData);
                    }
                    else
                    {
                        PopulateDataColumn(dataRow, rowDict, column);
                    }
                }

                dataTable.Rows.Add(dataRow);
            }
        }

        public static string GetSecurityNameColumnData(
            Dictionary<string, string> rowDict,
            Dictionary<string, string> columnHeadersMapping)
        {
            var securityColumn = columnHeadersMapping.FirstOrDefault(x =>
                x.Value.IndexOf(StachV3Constants.SecurityNameColumn, StringComparison.OrdinalIgnoreCase) >= 0);

            if (!string.IsNullOrEmpty(securityColumn.Key) && rowDict.ContainsKey(securityColumn.Key))
            {
                return rowDict[securityColumn.Key] ?? string.Empty;
            }

            return string.Empty;
        }

        public static int GetLevelIndex(
            Dictionary<string, string> rowDict,
            Dictionary<string, string> columnHeadersMapping)
        {
            var levelColumn = columnHeadersMapping.FirstOrDefault(x =>
                x.Key.IndexOf(StachV3Constants.LevelNameColumn, StringComparison.OrdinalIgnoreCase) >= 0);

            if (!string.IsNullOrEmpty(levelColumn.Key) && rowDict.ContainsKey(levelColumn.Key))
            {
                if (int.TryParse(rowDict[levelColumn.Key], out int levelIndex))
                {
                    return levelIndex;
                }
            }

            return 0;
        }

        public static void PopulateGroupColumn(
            DataTable dataTable,
            DataRow dataRow,
            string columnName,
            int levelIndex,
            int rowIndex,
            string securityNameColumnData)
        {
            if (int.TryParse(columnName.Replace(StachV3Constants.GroupPrefix, string.Empty), out int groupIndex))
            {
                if (levelIndex == groupIndex)
                {
                    dataRow[columnName] = securityNameColumnData;
                }
                else if (rowIndex > 0 && levelIndex > 0 && groupIndex < levelIndex)
                {
                    dataRow[columnName] = dataTable.Rows[rowIndex - 1][columnName];
                }
            }
        }

        public static void PopulateDataColumn(
            DataRow dataRow,
            Dictionary<string, string> rowDict,
            KeyValuePair<string, string> column)
        {
            var columnData = rowDict.ContainsKey(column.Key) ? rowDict[column.Key] : null;
            var columnName = column.Value;

            if (columnData != null && !string.IsNullOrEmpty(columnName))
            {
                dataRow[columnName] = columnData;
            }
        }

        public static void RemoveIgnoredColumns(DataTable dataTable)
        {
            foreach (var column in StachV3Constants.IgnoredColumns)
            {
                if (dataTable.Columns.Contains(column))
                {
                    dataTable.Columns.Remove(column);
                }
            }
        }

        public static long[] GetLevels(List<Dictionary<string, string>> rows)
        {
            if (rows == null || rows.Count == 0)
            {
                return Array.Empty<long>();
            }

            var levels = new HashSet<long>();

            foreach (var row in rows)
            {
                if (row.ContainsKey(StachV3Constants.LevelNameColumn) &&
                    long.TryParse(row[StachV3Constants.LevelNameColumn], out long level))
                {
                    levels.Add(level);
                }
            }

            return levels.ToArray();
        }

        public static string GetStachV3Properties(JObject source, string property)
        {
            if (source.TryGetValue(property, out JToken token) && token != null)
            {
                return token.ToString();
            }
            return string.Empty;
        }

        public static string GetStachV3Properties(JObject source, string property, string subProperty)
        {
            if (source.TryGetValue(property, out JToken token) && token != null)
            {
                var subToken = token[subProperty];
                if (subToken != null)
                {
                    return subToken.ToString();
                }
            }
            return string.Empty;
        }
    }
}