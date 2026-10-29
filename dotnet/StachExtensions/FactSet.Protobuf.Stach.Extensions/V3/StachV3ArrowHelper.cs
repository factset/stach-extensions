using Apache.Arrow;
using Apache.Arrow.Ipc;
using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;
using StachV3 = FactSet.Protobuf.Stach.V3;

namespace FactSet.Protobuf.Stach.Extensions.V3
{
    internal static class StachV3ArrowHelper
    {
        public static void BuildGroupColumns(byte[] arrowBytes, ConversionContext ctx)
        {
            var levels = GetLevels(arrowBytes);
            if (levels?.Length > 0)
            {
                var maxLevel = levels.Max();
                for (int i = 0; i <= maxLevel; i++)
                {
                    var levelColumnName = $"{StachV3Constants.GroupPrefix}{i}";
                    if (!ctx.ColumnHeadersMapping.ContainsKey(levelColumnName))
                        ctx.ColumnHeadersMapping.Add(levelColumnName, levelColumnName);
                }
            }
        }

        public static void ParseTableMetadata(IDictionary<string, string> metadata, ConversionContext ctx)
        {
            if (metadata.TryGetValue(ArrowSchemaMetadataNames.Table, out var tableObject))
            {
                ctx.StachTable = StachV3.Table.Parser.ParseFrom(System.Convert.FromBase64String(tableObject)) ?? new StachV3.Table();
            }
        }

        public static void ParseViewsMetadata(IDictionary<string, string> metadata, ConversionContext ctx)
        {
            if (metadata.TryGetValue(ArrowSchemaMetadataNames.Views, out var viewObject))
            {
                var views = StachV3.Views.Parser.ParseFrom(System.Convert.FromBase64String(viewObject)) ?? new StachV3.Views();
                ctx.ViewHeaders = views?.Views_?.FirstOrDefault()?.Table?.Headers?
                                  .ToDictionary(kvp => kvp.Key, kvp => kvp.Value) ?? new Dictionary<string, string>();
            }
        }

        public static void BuildConcatColumnsList(IDictionary<string, string> metadata, ConversionContext ctx)
        {
            if (!metadata.TryGetValue(ArrowSchemaMetadataNames.MultiLevelHeadersTable, out var multiLevelHeadersTableObject))
                return;

            var multiLevelHeadersArrayDecoded = System.Convert.FromBase64String(multiLevelHeadersTableObject);
            if (multiLevelHeadersArrayDecoded?.Length == 0)
                return;

            try
            {
                var multiLevelHeaders = GenerateMultiLevelHeaders(multiLevelHeadersArrayDecoded);
                if (multiLevelHeaders == null)
                    return;

                var multiLevelRef = ctx.StachTable?.SplitResult?.MultiLevelHeadersTableReference;
                bool hasViewHeaders = ctx.ViewHeaders?.Count > 0;
                var separatorChars = StachV3Constants.ColumnSeparator.ToCharArray(); // cache: avoids allocation per row

                // Pre-resolve column names and arrow arrays once — they are row-invariant
                var mlhColumns = new (string Name, IArrowArray Array)[multiLevelHeaders.ColumnCount];
                for (int i = 0; i < multiLevelHeaders.ColumnCount; i++)
                {
                    var col = multiLevelHeaders.Column(i);
                    mlhColumns[i] = (col.Name, col.Data.ArrowArray(0));
                }

                for (int rowIndex = 0; rowIndex < multiLevelHeaders.RowCount; rowIndex++)
                {
                    var concatColumnValue = string.Empty;
                    var primaryKeyColumn = string.Empty;

                    foreach (var (colName, array) in mlhColumns)
                    {
                        var headerColumnValue = GetColumnValue(array, rowIndex);

                        if (colName == multiLevelRef)
                            primaryKeyColumn = headerColumnValue;

                        if (!string.IsNullOrEmpty(headerColumnValue))
                        {
                            if (hasViewHeaders && ctx.ViewHeaders.TryGetValue(headerColumnValue, out var mapped))
                                headerColumnValue = mapped ?? headerColumnValue;

                            concatColumnValue = headerColumnValue + StachV3Constants.ColumnSeparator + concatColumnValue;
                        }
                    }

                    if (!string.IsNullOrEmpty(primaryKeyColumn))
                        ctx.ConcatColumnsList.Add(primaryKeyColumn, concatColumnValue.TrimEnd(separatorChars));
                }
            }
            catch (Exception)
            {
                // Handle exception
            }
        }

        public static void MapColumnHeaders(IList<Field> fields, ConversionContext ctx)
        {
            bool hasConcatColumns = ctx.ConcatColumnsList.Count > 0;
            bool hasViewHeaders = ctx.ViewHeaders.Count > 0;
            foreach (var field in fields)
            {
                string headerName = field.Name;
                if (hasConcatColumns && ctx.ConcatColumnsList.TryGetValue(field.Name, out var concatName))
                    headerName = concatName;
                else if (hasViewHeaders && ctx.ViewHeaders.TryGetValue(field.Name, out var viewName))
                    headerName = viewName;

                ctx.ColumnHeadersMapping.Add(field.Name, headerName);
            }
        }

        public static void BuildDataTableColumns(ConversionContext ctx)
        {
            ctx.DataTable.Columns.AddRange(
                ctx.ColumnHeadersMapping.Select(kvp => new DataColumn(kvp.Value, typeof(string))).ToArray()
            );
        }

        public static void PopulateDataTable(IArrowArrayStream reader, ConversionContext ctx)
        {
            // Pre-compute once: security and level column keys (avoid O(n) search per row)
            var securityColumnKey = ctx.ColumnHeadersMapping
                .FirstOrDefault(x => x.Value != null && x.Value.IndexOf(StachV3Constants.SecurityNameColumn, StringComparison.OrdinalIgnoreCase) >= 0).Key;
            var levelColumnKey = ctx.ColumnHeadersMapping
                .FirstOrDefault(x => x.Key != null && x.Key.IndexOf(StachV3Constants.LevelNameColumn, StringComparison.OrdinalIgnoreCase) >= 0).Key;

            // Pre-split into group columns (with pre-parsed index) and data columns.
            // Avoids repeated groupPrefix checks and int.Parse on every row.
            var groupColumnEntries = ctx.ColumnHeadersMapping
                .Where(kvp => kvp.Key.StartsWith(StachV3Constants.GroupPrefix))
                .Select(kvp => (Key: kvp.Key, GroupIndex: int.Parse(kvp.Key.Substring(StachV3Constants.GroupPrefix.Length))))
                .ToList();

            // Array: fixed schema means fixed size; pre-allocated and reused across batches
            var dataColumnEntries = ctx.ColumnHeadersMapping
                .Where(kvp => !kvp.Key.StartsWith(StachV3Constants.GroupPrefix) && !string.IsNullOrEmpty(kvp.Key) && !string.IsNullOrEmpty(kvp.Value))
                .Select(kvp => (Key: kvp.Key, Name: kvp.Value))
                .ToArray();

            var resolvedDataColumns = new (string Name, IArrowArray Data)[dataColumnEntries.Length];

            // BeginLoadData suspends index/constraint updates during bulk insert;
            // EndLoadData rebuilds them once at the end
            ctx.DataTable.BeginLoadData();
            try
            {
                RecordBatch recordBatch;
                while ((recordBatch = reader.ReadNextRecordBatchAsync().Result) != null)
                {
                    // Resolve column arrays once per batch — column data is stable within a batch
                    IArrowArray securityNameColumnData = !string.IsNullOrEmpty(securityColumnKey) && IsColumnExist(recordBatch, securityColumnKey)
                        ? recordBatch.Column(securityColumnKey)
                        : null;

                    // Cast directly to Int64Array — avoids GetColumnValue string round-trip on every level read
                    Int64Array levelColumnAsInt64 = !string.IsNullOrEmpty(levelColumnKey) && IsColumnExist(recordBatch, levelColumnKey)
                        ? recordBatch.Column(levelColumnKey) as Int64Array
                        : null;

                    // Fill pre-allocated array in-place — no new list or LINQ per batch
                    for (int i = 0; i < dataColumnEntries.Length; i++)
                        resolvedDataColumns[i] = (dataColumnEntries[i].Name, recordBatch.Column(dataColumnEntries[i].Key));

                    for (int rowIndex = 0; rowIndex < recordBatch.Length; rowIndex++)
                    {
                        var dataRow = ctx.DataTable.NewRow();

                        int levelIndex = levelColumnAsInt64 != null
                            ? (int)(levelColumnAsInt64.GetValue(rowIndex) ?? 0L)
                            : 0;

                        // fill data columns
                        for (int i = 0; i < resolvedDataColumns.Length; i++)
                        {
                            var (name, data) = resolvedDataColumns[i];
                            if (data != null)
                                dataRow[name] = GetColumnValue(data, rowIndex);
                        }

                        // fill group columns
                        foreach (var (colKey, groupIndex) in groupColumnEntries)
                        {
                            if (levelIndex == groupIndex)
                            {
                                dataRow[colKey] = securityNameColumnData != null
                                    ? GetColumnValue(securityNameColumnData, rowIndex)
                                    : string.Empty;
                            }
                            else if (rowIndex > 0 && levelIndex > 0 && groupIndex < levelIndex)
                            {
                                dataRow[colKey] = securityNameColumnData != null
                                    ? ctx.DataTable.Rows[rowIndex - 1][colKey]
                                    : (object)string.Empty;
                            }
                        }

                        ctx.DataTable.Rows.Add(dataRow);
                    }
                }
            }
            finally
            {
                ctx.DataTable.EndLoadData();
            }
        }

        public static void RemoveIgnoredColumns(ConversionContext ctx)
        {
            if (StachV3Constants.IgnoredColumns?.Count > 0)
            {
                foreach (var column in StachV3Constants.IgnoredColumns)
                {
                    if (ctx.DataTable.Columns.Contains(column))
                        ctx.DataTable.Columns.Remove(column);
                }
            }
        }

        public static Apache.Arrow.Table GenerateMultiLevelHeaders(byte[] multiLevelHeadersArrayDecoded)
        {
            var multiLevelHeadersRecords = new List<RecordBatch>();
            Apache.Arrow.Table mlhTable = null;

            try
            {
                using (MemoryStream memoryStream = new MemoryStream(multiLevelHeadersArrayDecoded))
                using (var arrowFileReader = new ArrowFileReader(memoryStream, true))
                {
                    var rb = arrowFileReader.ReadNextRecordBatchAsync().Result;
                    while (rb != null)
                    {
                        multiLevelHeadersRecords.Add(rb);
                        rb = arrowFileReader.ReadNextRecordBatchAsync().Result;
                    }

                    mlhTable = ReadMultiLevelHeadersSchema(multiLevelHeadersRecords, arrowFileReader.Schema);
                }
            }
            catch (InvalidDataException ex)
            {
                if (ex.Message == "Invalid magic at offset <6>")
                {
                    using (MemoryStream memoryStream = new MemoryStream(multiLevelHeadersArrayDecoded))
                    using (var arrowStreamReader = new ArrowStreamReader(memoryStream, true))
                    {
                        var rb = arrowStreamReader.ReadNextRecordBatchAsync().Result;
                        while (rb != null)
                        {
                            multiLevelHeadersRecords.Add(rb);
                            rb = arrowStreamReader.ReadNextRecordBatchAsync().Result;
                        }

                        mlhTable = ReadMultiLevelHeadersSchema(multiLevelHeadersRecords, arrowStreamReader.Schema);
                    }
                }
            }

            return mlhTable;
        }

        public static Apache.Arrow.Table ReadMultiLevelHeadersSchema(List<RecordBatch> multiLevelHeadersRecords, Schema schema)
        {
            var nBatches = multiLevelHeadersRecords.Count;
            var nColumns = schema.FieldsLookup.Count;
            var columns = new List<Apache.Arrow.Column>(nColumns);
            var columnArrays = new List<Apache.Arrow.Array>(nBatches);

            for (var i = 0; i < nColumns; i++)
            {
                for (var j = 0; j < nBatches; j++)
                {
                    var colValue = ((RecordBatch)multiLevelHeadersRecords[j]).Column(i);
                    columnArrays.Add((Apache.Arrow.Array)colValue);
                }

                columns.Add(new Apache.Arrow.Column(schema.GetFieldByIndex(i), columnArrays));
                columnArrays.Clear();
            }

            return new Apache.Arrow.Table(schema, columns);
        }

        public static string GetColumnValue(IArrowArray columnArray, int rowIndex)
        {
            if (columnArray == null)
                return string.Empty;

            if (columnArray is Int8Array int8Array)
                return int8Array.GetValue(rowIndex)?.ToString();
            else if (columnArray is Int16Array int16Array)
                return int16Array.GetValue(rowIndex)?.ToString();
            else if (columnArray is Int32Array int32Array)
                return int32Array.GetValue(rowIndex)?.ToString();
            else if (columnArray is Int64Array int64Array)
                return int64Array.GetValue(rowIndex)?.ToString();
            else if (columnArray is StringArray stringArray)
                return stringArray.GetString(rowIndex);
            else if (columnArray is FloatArray floatArray)
                return floatArray.GetValue(rowIndex)?.ToString();
            else if (columnArray is DoubleArray doubleArray)
                return doubleArray.GetValue(rowIndex)?.ToString();
            else if (columnArray is Time32Array time32Array)
                return time32Array.GetValue(rowIndex)?.ToString();
            else if (columnArray is Time64Array time64Array)
                return time64Array.GetValue(rowIndex)?.ToString();
            else if (columnArray is Date32Array date32Array)
                return date32Array.GetValue(rowIndex)?.ToString();
            else if (columnArray is Date64Array date64Array)
                return date64Array.GetValue(rowIndex)?.ToString();
            else if (columnArray is BooleanArray boolArray)
                return boolArray.GetValue(rowIndex)?.ToString();
            else
                return string.Empty;
        }

        public static long[] GetLevels(byte[] arrowBytes)
        {
            var levels = new List<long>();
            var arrowStreamReader = GetArrowStream(arrowBytes) as ArrowStreamReader;
            if (arrowStreamReader == null)
                throw new InvalidOperationException("Failed to create ArrowStreamReader from arrowBytes.");

            var recordBatch = arrowStreamReader.ReadNextRecordBatchAsync().Result;
            if (recordBatch != null)
            {
                int columnIndex = recordBatch.Schema.GetFieldIndex("level");
                if (columnIndex >= 0)
                {
                    var columnX = (Int64Array)recordBatch.Column(columnIndex);
                    for (int i = 0; i < columnX.Values.Length; i++)
                        levels.Add(columnX.Values[i]);
                }
            }
            return levels.ToArray();
        }

        public static bool IsArrowFile(Stream stream)
        {
            byte[] arrowFileMagic = new byte[] { (byte)'A', (byte)'R', (byte)'R', (byte)'O', (byte)'W', (byte)'1' };
            byte[] buffer = new byte[6];
            long originalPosition = stream.Position;
            stream.Seek(0, SeekOrigin.Begin);
            stream.Read(buffer, 0, 6);
            stream.Seek(originalPosition, SeekOrigin.Begin);
            return buffer.SequenceEqual(arrowFileMagic);
        }

        public static IArrowArrayStream GetArrowStream(byte[] arrowBytes)
        {
            if (arrowBytes == null)
                return null;

            // Do NOT use a 'using' here — the MemoryStream must stay alive for as long as
            // the returned reader is in use. The caller is responsible for disposing the reader,
            // which in turn disposes the underlying stream.
            var arrowStream = new MemoryStream(arrowBytes);
            return IsArrowFile(arrowStream)
                ? (IArrowArrayStream)new ArrowFileReader(arrowStream)
                : new ArrowStreamReader(arrowStream);
        }
        
        public static bool IsColumnExist(RecordBatch recordBatch, string columnName)
        {
            if (recordBatch != null)
            {
                int columnIndex = recordBatch.Schema.GetFieldIndex(columnName);
                if (columnIndex >= 0)
                    return true;
            }
            return false;
        }

    }

    // Holds all mutable state for a single conversion call.
    // Using a context object prevents state pollution between concurrent calls
    // and avoids shared static fields.
    public sealed class ConversionContext
    {
        public DataTable DataTable { get; } = new DataTable();
        public Dictionary<string, string> ColumnHeadersMapping { get; } = new Dictionary<string, string>();
        public Dictionary<string, string> ViewHeaders { get; set; } = new Dictionary<string, string>();
        public StachV3.Table StachTable { get; set; } = new StachV3.Table();
        public Dictionary<string, string> ConcatColumnsList { get; } = new Dictionary<string, string>();
    }
}
