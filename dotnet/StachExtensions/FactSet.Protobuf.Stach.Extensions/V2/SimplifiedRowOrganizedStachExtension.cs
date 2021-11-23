using System;
using System.Collections.Generic;
using System.Dynamic;
using System.Linq;
using FactSet.Protobuf.Stach.V2;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public class SimplifiedRowOrganizedStachExtension : RowOrganizedStachExtension, ISimplifiedRowStachExtension
    {
        private RowOrganizedPackage pkg;
        private const string ColumnIdPrefix = "col_";
        
        public SimplifiedRowOrganizedStachExtension(RowOrganizedPackage pkg) : base(pkg)
        {
            this.pkg = pkg;
        }
        
        public List<dynamic> ConvertToDynamicObjects()
        {
            var output = new List<dynamic>();
            
            foreach (var table in pkg.Tables.Values)
            {
                var headerRow = new List<dynamic>(); // List of keys for ExpandoObject
                foreach (var columnDefinition in table.Definition.Columns)
                {
                    var description = string.IsNullOrWhiteSpace(columnDefinition.Description)
                        ? columnDefinition.Name
                        : columnDefinition.Description;
                    headerRow.Add(description);
                }

                foreach (var dataRow in table.Data.Rows)
                {
                    var data = new ExpandoObject() as IDictionary<string, dynamic>;
                    for (var i = 0; i < dataRow.Values.Fields.Count; i++)
                    {
                        data.Add(headerRow[i], StachUtilities.ValueToObject(dataRow.Values.Fields[ColumnIdPrefix + i]));
                    }
                    output.Add(data);
                }
            }

            return output;
        }

        public List<dynamic> ConvertToTransposedDynamicObjects()
        {
            var transposedOutput = new List<dynamic>();

            foreach (var table in pkg.Tables.Values)
            {
                var headerKeys = new List<dynamic>();// List of key values
                
                string key = null;
                var dimensionColumnIds = new List<string>();
                if (table.Definition.Columns.Count(c => c.IsDimension) == 0)
                    table.Definition.Columns.First().IsDimension = true; // If no dimension column, then assuming first column as dimension column
                
                foreach (var column in table.Definition.Columns.Where(c=> c.IsDimension))
                {
                    key +=  (column.Description?? column.Name) + " | "; // concatenating dimension columns' names to add in keys list
                    dimensionColumnIds.Add(column.Id);
                }
                headerKeys.Add(key?.Remove(key.LastIndexOf(" | ", StringComparison.OrdinalIgnoreCase)));
                
                foreach (var row in table.Data.Rows)
                {
                    string keyValue = null;
                    foreach (var id in dimensionColumnIds)
                    {
                        var rowCellValue = StachUtilities.ValueToString(row.Values.Fields[id]);
                        if (rowCellValue == null) continue;
                        keyValue += rowCellValue + " | "; // concatenating dimension columns' values to add in keys list
                    }
                    headerKeys.Add(keyValue?.Remove(keyValue.LastIndexOf(" | ", StringComparison.OrdinalIgnoreCase)));
                }
                
                foreach (var column in table.Definition.Columns.Where(c=> c.IsDimension != true))
                {
                    var data = new ExpandoObject() as IDictionary<string, dynamic>;
                    data.Add(headerKeys[0], column.Description ?? column.Name);
                    int j = 1;
                    for (var i = 0; i < headerKeys.Count - 1; i++)
                    {
                        if (data.ContainsKey(headerKeys[i + 1])) //logic to handle duplicate key values
                        {
                            headerKeys[i + 1] += "_"+j;
                            j++;
                        }
                        data.Add(headerKeys[i+1], StachUtilities.ValueToObject(table.Data.Rows[i].Values.Fields[column.Id]));
                    }
                    
                    transposedOutput.Add(data);
                }
                
            }
            
            return transposedOutput;
        }
    }
}