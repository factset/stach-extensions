using System.Collections.Generic;
using System.Linq;
using FactSet.Protobuf.Stach.Extensions.Models;
using FactSet.Protobuf.Stach.V2;
using System;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public class RowOrganizedStachExtension : IStachExtension
    {
        private RowOrganizedPackage pkg;

        public RowOrganizedStachExtension(RowOrganizedPackage pkg)
        {
            this.pkg = pkg;
        }
        
        public List<Models.Table> ConvertToTable()
        {
            var tables = new List<Models.Table>();

            foreach (var table in pkg.Tables.Values)
            {
                tables.Add(GenerateTable(table));
            }

            return tables;
        }

        private Models.Table GenerateTable(RowOrganizedPackage.Types.Table table)
        {
            var finalTable = new Models.Table
            {
                Rows = new List<Row>(),
                Metadata = new Dictionary<string, string>(),
                RawMetadata = new Dictionary<string, List<Google.Protobuf.WellKnownTypes.Value>>()
            };
            
            var rowIndex = 0;
            var firstRow = table.Data.Rows[0];

            // If first row is not header row, considering it as simplified row format.
            if (firstRow.RowType != RowOrganizedPackage.Types.Row.Types.RowType.Header)
            {
                var headerRow = new Row {Cells = new List<string>()};
                foreach (var columnDefinition in table.Definition.Columns)
                {
                    var description = string.IsNullOrWhiteSpace(columnDefinition.Description)
                        ? columnDefinition.Name
                        : columnDefinition.Description;
                    headerRow.Cells.Add(description);
                }

                headerRow.isHeader = true;
                finalTable.Rows.Add(headerRow);
            }
            
            // List to store the info about the values that needs to be spread based on rowspan
            var rowSpanSpreadList = new List<RowSpanSpread>();
            for (; rowIndex < table.Data.Rows.Count; rowIndex++)
            {
                var currentRow = table.Data.Rows[rowIndex];
                if (currentRow.RowType == RowOrganizedPackage.Types.Row.Types.RowType.Header)
                {
                    var headerRow = new Row {Cells = new List<string>()};
                    var index = 0;    // The index of values in the header row cells list.
                    var position = 0;    // The actual column position (by considering the rowspan, colspan spread).

                    var headerRowValues = currentRow.Cells.Values.ToList();
                    var headerCellDetailsArray = currentRow.HeaderCellDetails.Values.ToList();

                    // Checking and adding values at the start of the row if any based on the rowspan info
                    // available from the previously processed rows.
                    var spreadValuesList = StachUtilities.CheckAddRowSpanItems(position, rowIndex, rowSpanSpreadList);
                    if (spreadValuesList != null)
                    {
                        position += spreadValuesList.Count;
                        foreach (var value in spreadValuesList)
                        {
                            headerRow.Cells.Add(StachUtilities.ValueToString(value) ?? string.Empty);
                        }
                    }

                    foreach (var cellValue in headerRowValues)
                    {
                        var rowSpan = headerCellDetailsArray[index].Rowspan <= 1
                            ? 1
                            : headerCellDetailsArray[index].Rowspan;
                        var colSpan = headerCellDetailsArray[index].Colspan <= 1
                            ? 1
                            : headerCellDetailsArray[index].Colspan;

                        // If rowspan > 1, the value needs to be spread across multiple rows at the current position.
                        // storing the position, rowspan number and the actual value to the rowSpanSpreadList.
                        if (rowSpan > 1)
                        {
                            rowSpanSpreadList.Add(new RowSpanSpread(position, rowSpan, colSpan, cellValue));
                        }

                        for (var i = 0; i < colSpan; i++)
                        {
                            headerRow.Cells.Add(StachUtilities.ValueToString(cellValue));
                            position++;
                            
                            // After incrementing column position from above line, checking if any value has to be
                            // added at the new column position based on row spread list
                            spreadValuesList = StachUtilities.CheckAddRowSpanItems(position, rowIndex, rowSpanSpreadList);
                            if (spreadValuesList != null)
                            {
                                position = position + spreadValuesList.Count;
                                foreach (var value in spreadValuesList)
                                {
                                    headerRow.Cells.Add(StachUtilities.ValueToString(value) ?? string.Empty);
                                }
                            }
                        }

                        headerRow.isHeader = true;
                        index++;
                    }
                    
                    finalTable.Rows.Add(headerRow);
                }
                else
                {
                    var dataRow = new Row {Cells = new List<string>()};
                    var rowDataMap = table.Data.Rows[rowIndex].Values.Fields;

                    foreach (var columnDefinition in table.Definition.Columns)
                    {
                       var value =  StachUtilities.ValueToString(rowDataMap[columnDefinition.Id]) ?? columnDefinition.Format?.NullFormat;
                       dataRow.Cells.Add(value ?? string.Empty);
                    }
                    
                    finalTable.Rows.Add(dataRow);
                }
            }

            foreach (var metadataItem in table.Data.TableMetadata)
            {
                var metadataValueString = StachUtilities.ValueToString(metadataItem.Value.Value);
                var metadataValue = metadataItem.Value.Value;

                finalTable.Metadata.Add(metadataItem.Key, metadataValueString);

                List<Google.Protobuf.WellKnownTypes.Value> valuesList = new List<Google.Protobuf.WellKnownTypes.Value>();
                if (metadataValue.KindCase.ToString() == "ListValue")
                {
                    foreach (Google.Protobuf.WellKnownTypes.Value val in metadataValue.ListValue.Values)
                    {
                        valuesList.Add(val);
                    }
                }
                else if (metadataValue.KindCase.ToString() == "StringValue")
                {
                    // parsing metadataItem.Value into a List of values
                    string valString = metadataValue.ToString();
                    string[] values = valString.Split(new string[] { "\", \"" }, StringSplitOptions.None);
                    foreach (string val in values)
                    {
                        char[] charsToTrim = { '[', ' ', ']', '\"' };
                        string trimmed = val.Trim(charsToTrim);
                        valuesList.Add(Google.Protobuf.WellKnownTypes.Value.ForString(trimmed));
                    }
                }

                finalTable.RawMetadata.Add(metadataItem.Key, valuesList);
            }

            return finalTable;
        }
    }
}