using System.Collections.Generic;
using System.Linq;
using FactSet.Protobuf.Stach.Extensions.Models;
using FactSet.Protobuf.Stach.V2;

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
                Metadata = new Dictionary<string, string>()
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
                    var index = 0;
                    var position = 0;

                    var headerRowValues = currentRow.Cells.Values.ToList();
                    var headerCellDetailsArray = currentRow.HeaderCellDetails.Values.ToList();

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

                        if (rowSpan > 1)
                        {
                            rowSpanSpreadList.Add(new RowSpanSpread(position, rowSpan, colSpan, cellValue));
                        }

                        for (var i = 0; i < colSpan; i++)
                        {
                            headerRow.Cells.Add(StachUtilities.ValueToString(cellValue));
                            position++;
                            
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
                var metadataValue = StachUtilities.ValueToString(metadataItem.Value.Value);
                finalTable.Metadata.Add(metadataItem.Key, metadataValue);
            }

            return finalTable;
        }
    }
}