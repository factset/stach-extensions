using FactSet.Protobuf.Stach.Extensions.V3;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;

namespace FactSet.Protobuf.Stach.Extensions
{
    public static class StachV3ExtensionFactory
    {  
        public static DataSet ConvertJsonToTable(string jsonFileContent)
        {
            var dataSet = new DataSet();

            if (string.IsNullOrEmpty(jsonFileContent))
            {
                return dataSet;
            }

            var stachJObject = JObject.Parse(jsonFileContent);
            if (stachJObject == null)
            {
                return null;
            }

            var dataTable = new DataTable();
            var columnHeadersMapping = new Dictionary<string, string>();

            // Parse schema components
            var stachTable = StachV3JsonHelper.ParseTableSchema(stachJObject);
            var customStachV3Views = StachV3JsonHelper.ParseViewsSchema(stachJObject);
            var customStachV3Table = StachV3JsonHelper.ParseColumnsAndRows(stachJObject);
            var multiLevelHeaders = StachV3JsonHelper.ParseMultiLevelHeaders(stachJObject);

            // Build column headers mapping
            StachV3JsonHelper.AddGroupColumns(customStachV3Table.Rows, columnHeadersMapping);
            var concatColumnsList = StachV3JsonHelper.BuildConcatenatedColumns(multiLevelHeaders, stachTable, customStachV3Views);
            StachV3JsonHelper.MapColumnHeaders(customStachV3Table, customStachV3Views, concatColumnsList, columnHeadersMapping);

            // Create DataTable structure
            StachV3JsonHelper.CreateDataTableColumns(dataTable, columnHeadersMapping);
            StachV3JsonHelper.PopulateDataTableRows(dataTable, customStachV3Table, columnHeadersMapping);
            StachV3JsonHelper.RemoveIgnoredColumns(dataTable);

            dataSet.Tables.Add(dataTable);
            return dataSet;
        }

        public static DataSet ConvertArrowStreamToTable(byte[] arrowBytes)
        {
            var ctx = new ConversionContext();

            // using ensures the reader (and its underlying MemoryStream) is disposed after conversion
            using (var reader = StachV3ArrowHelper.GetArrowStream(arrowBytes))
            {
                if (reader == null)
                    return null;

                var schema = reader.Schema;

                StachV3ArrowHelper.BuildGroupColumns(arrowBytes, ctx);

                if (schema?.HasMetadata == true)
                {
                    var metadata = (IDictionary<string, string>)schema.Metadata;
                    StachV3ArrowHelper.ParseTableMetadata(metadata, ctx);
                    StachV3ArrowHelper.ParseViewsMetadata(metadata, ctx);
                    StachV3ArrowHelper.BuildConcatColumnsList(metadata, ctx);
                    StachV3ArrowHelper.MapColumnHeaders(schema.FieldsList.ToList(), ctx);
                }

                StachV3ArrowHelper.BuildDataTableColumns(ctx);
                StachV3ArrowHelper.PopulateDataTable(reader, ctx);
                StachV3ArrowHelper.RemoveIgnoredColumns(ctx);
            }

            var dataSet = new DataSet();
            dataSet.Tables.Add(ctx.DataTable);
            return dataSet;
        }
        
        public static DataSet ConvertArrowFileToTable(string filePath)
        {
            if (!File.Exists(filePath))
                return null;

            byte[] arrowBytes = File.ReadAllBytes(filePath);
            return ConvertArrowStreamToTable(arrowBytes);
        }
    }
}