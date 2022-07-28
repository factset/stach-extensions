using System.Collections.Generic;
using System.Linq;
using Newtonsoft.Json.Linq;
using System;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public static class ColumnOrganizedDecompress
    {
        /// <summary>
        /// Get a list of primary table ids.
        /// </summary>
        /// <param name="package">The stachdata in string json format</param>
        /// <returns>List of table ids</returns>
        public static List<String> GetPrimaryTableIds(string package) {
            JObject packageJson = JObject.Parse(package);

            JArray idJArray = (JArray)packageJson["primaryTableIds"];
            
            return idJArray.ToObject<List<String>>();
        }

        /// <summary>
        /// Decompress column organized stach data
        /// </summary>
        /// <param name="package">The stachdata in string json format</param>
        /// <param name="primaryTableId">The specific table id to decompress</param>
        /// <returns>Decompressed stachdata</returns>
        public static string Decompress(string package) {
            JObject packageJson = JObject.Parse(package);

            List<String> tableIds = ColumnOrganizedDecompress.GetPrimaryTableIds(package);

            foreach(String tableId in tableIds) {
                packageJson = ColumnOrganizedDecompress.Decompress(packageJson, tableId);
            }

            return packageJson.ToString();
        }

        /// <summary>
        /// Decompress column organized stach data
        /// </summary>
        /// <param name="package">The stachdata in json format</param>
        /// <param name="primaryTableId">The specific table id to decompress</param>
        /// <returns>Decompressed stachdata</returns>
        private static JObject Decompress(JObject package, string primaryTableId)
        {
            JObject columns = (JObject)package["tables"][primaryTableId]["data"]["columns"];
            List<String> columnIDs = columns.Properties().Select(p => p.Name).ToList();

            foreach (String columnID in columnIDs) {
                JObject column = (JObject)columns[columnID];
                if (!column.ContainsKey("ranges"))
                    continue;
                ColumnOrganizedDecompress.decompressColumn(column);
            }

            return package;
        }

        private static void decompressColumn(JObject column)
        {
            JObject ranges = (JObject)column["ranges"];
            JArray compressedValues = (JArray)column["values"];

            JArray decompressedValues = new JArray();
            for(int compressedIndex = 0; compressedIndex < compressedValues.Count; compressedIndex++) {
                string currentDecompressedIndex = (decompressedValues.Count).ToString();

                if(!ranges.ContainsKey(currentDecompressedIndex)) {
                    decompressedValues.Add(compressedValues[compressedIndex]);
                    continue;
                }

                int rangeValue = (int) ranges[currentDecompressedIndex];
                for(int rangeIndex = 0; rangeIndex < rangeValue; rangeIndex++) {
                    decompressedValues.Add(compressedValues[compressedIndex]);
                }
            }

            column.Remove("ranges");
            column["values"] = decompressedValues;
        }
    }
}


