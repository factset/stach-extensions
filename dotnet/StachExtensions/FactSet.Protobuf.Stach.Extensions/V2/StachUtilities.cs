﻿using System.Collections.Generic;
using System.Linq;
using FactSet.Protobuf.Stach.Extensions.Models;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using Newtonsoft.Json.Linq;
using System;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public static class StachUtilities
    {
        /// <summary>
        /// Returns stringified value for the Protobuf Value object
        /// </summary>
        /// <param name="value"></param>
        /// <returns>String value</returns>
        public static string ValueToString(Value value)
        {
            switch (value.KindCase)
            {
                case Value.KindOneofCase.BoolValue:
                    return value.BoolValue.ToString();
                
                case Value.KindOneofCase.ListValue:
                    return value.ListValue.ToString();

                case Value.KindOneofCase.NumberValue:
                    return value.NumberValue.ToString();
                
                case Value.KindOneofCase.StringValue:
                    return value.StringValue;
                
                case Value.KindOneofCase.StructValue:
                    return JsonFormatter.Default.Format(value.StructValue);
                
                case Value.KindOneofCase.NullValue:
                case Value.KindOneofCase.None:
                default:
                    return null;
            }
        }

        /// <summary>
        /// Returns the underlying raw object from the protobuf Value object.
        /// For Lists and Struct, we return the stringified values.
        /// </summary>
        /// <param name="value"></param>
        /// <returns>Raw object value</returns>
        public static object ValueToObject(Value value)
        {
            switch (value.KindCase)
            {
                case Value.KindOneofCase.BoolValue:
                    return value.BoolValue;
                
                case Value.KindOneofCase.ListValue:
                    return value.ListValue.ToString();
                
                case Value.KindOneofCase.NumberValue:
                    return value.NumberValue;
                
                case Value.KindOneofCase.StringValue:
                    return value.StringValue;
                
                case Value.KindOneofCase.StructValue:
                    return JsonFormatter.Default.Format(value.StructValue);
                
                case Value.KindOneofCase.NullValue:
                case Value.KindOneofCase.None:
                default:
                    return null;
            }
        }

        /// <summary>
        /// Checks if values needs to be added at the given row and given position based on rowspan information. If values needs to be added,
        /// builds the list of values to be added at the position and returns it.
        /// </summary>
        /// <param name="position">Position at which the values should be added.</param>
        /// <param name="rowIndex">The current row index</param>
        /// <param name="rowSpanSpreadList">List containing the items that needs to be spread or added along with the position and count.</param>
        /// <returns>List of values to be added at the given position if any</returns>
        public static List<Value> CheckAddRowSpanItems(int position, int rowIndex,
            List<RowSpanSpread> rowSpanSpreadList)
        {
            var spreadValuesList = new List<Value>();

            if (rowSpanSpreadList.Count == 0)
            {
                return null;
            }

            var rowSpanSpreadItem = rowSpanSpreadList.FirstOrDefault(x => x.Position == position && rowIndex < x.RowSpan);

            if (rowSpanSpreadItem != null)
            {
                for (int i = 0; i < rowSpanSpreadItem.ColSpan; i++)
                {
                    // Appending the value to be spread and updating the column position by 1.
                    spreadValuesList.Add(rowSpanSpreadItem.Value);
                    position++;
                }

                // Checking and adding if value has to be added at the updated column position recursively.
                var recursiveSpreadValuesList =
                    StachUtilities.CheckAddRowSpanItems(position, rowIndex, rowSpanSpreadList);

                if (recursiveSpreadValuesList != null)
                {
                    spreadValuesList.AddRange(recursiveSpreadValuesList);
                }

                return spreadValuesList;
            }
            
            return null;
        }

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

            List<String> tableIds = StachUtilities.GetPrimaryTableIds(package);

            foreach(String tableId in tableIds) {
                packageJson = StachUtilities.Decompress(packageJson, tableId);
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
                StachUtilities.decompressColumn(column);
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
