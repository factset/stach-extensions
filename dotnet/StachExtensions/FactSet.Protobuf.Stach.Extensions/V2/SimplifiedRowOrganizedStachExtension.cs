using System;
using System.Collections.Generic;
using System.Linq;
using FactSet.Protobuf.Stach.V2;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public class SimplifiedRowOrganizedStachExtension : RowOrganizedStachExtension, ISimplifiedRowStachExtension
    {
        private RowOrganizedPackage pkg;
        
        public SimplifiedRowOrganizedStachExtension(RowOrganizedPackage pkg) : base(pkg)
        {
            this.pkg = pkg;
        }
        
        public List<dynamic> ConvertToDynamicObject()
        {
            var output = new List<dynamic>();
            
            foreach (var table in pkg.Tables.Values)
            {
                var headerRow = new List<dynamic>();
                foreach (var columnDefinition in table.Definition.Columns)
                {
                    var description = string.IsNullOrWhiteSpace(columnDefinition.Description)
                        ? columnDefinition.Name
                        : columnDefinition.Description;
                    headerRow.Add(description);
                }

                foreach (var dataRow in table.Data.Rows)
                {
                    dynamic data = new Dictionary<dynamic, dynamic>();
                    for (var i = 0; i < dataRow.Values.Fields.Count; i++)
                    {
                        data.Add(headerRow[i], StachUtilities.ValueToObject(dataRow.Values.Fields["col_"+i]));
                    }
                    output.Add(data);
                }
            }

            return output;
        }

        public List<dynamic> ConvertToTransposedDynamicObject()
        {
            var transposedOutput = new List<dynamic>();

            foreach (var table in pkg.Tables.Values)
            {
                var headerKeys = new List<dynamic>();
                
                string key = null;
                var dimensionColumnIds = new List<string>();
                if (table.Definition.Columns.Count(c => c.IsDimension) == 0)
                    table.Definition.Columns.First().IsDimension = true;
                
                foreach (var column in table.Definition.Columns)
                {
                    if (!column.IsDimension) continue;
                    key +=  (column.Description?? column.Name) + " | ";
                    dimensionColumnIds.Add(column.Id);
                }
                headerKeys.Add(key?.Remove(key.LastIndexOf(" | ", StringComparison.OrdinalIgnoreCase)));
                
                foreach (var row in table.Data.Rows)
                {
                    string keyValue = null;
                    foreach (var id in dimensionColumnIds)
                    {
                        if (StachUtilities.ValueToString(row.Values.Fields[id]) == null) continue;
                        keyValue += StachUtilities.ValueToString(row.Values.Fields[id]) + " | ";
                    }
                    headerKeys.Add(keyValue?.Remove(keyValue.LastIndexOf(" | ", StringComparison.OrdinalIgnoreCase)));
                }
                
                foreach (var column in table.Definition.Columns)
                {
                    if (dimensionColumnIds.Contains(column.Id)) continue;
                    
                    var data = new Dictionary<string, dynamic>();
                    data.Add(headerKeys[0], column.Description ?? column.Name);
                    int j = 1;
                    for (var i = 0; i < headerKeys.Count - 1; i++)
                    {
                        if (data.ContainsKey(headerKeys[i + 1]))
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

        public string GetStringifiedCsv(List<dynamic> output)
        {
            string csv = null;
            foreach (var pair in output.First())
            {
                csv += pair.Key.ToString() + ",";
            }

            csv += "\n";
            foreach (var pair in output)
            {
                foreach (var item in pair)
                {
                    csv += item.Value?.ToString() + ",";
                }

                csv += "\n";
            }

            return csv;
        }
    }
}