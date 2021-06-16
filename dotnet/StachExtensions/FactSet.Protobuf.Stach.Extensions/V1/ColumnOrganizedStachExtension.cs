using System;
using System.Collections.Generic;
using System.Linq;
using FactSet.Protobuf.Stach.Extensions.Models;

namespace FactSet.Protobuf.Stach.Extensions.V1
{
    public class ColumnOrganizedStachExtension : IStachExtension
    {
        private readonly Package pkg;
        
        public ColumnOrganizedStachExtension(Package package)
        {
            this.pkg = package;
        }

        public List<Models.Table> ConvertToTable()
        {
            var tables = new List<Models.Table>();
            foreach (var primaryTableId in pkg.PrimaryTableIds)
            {
                tables.Add(GenerateTable(pkg, primaryTableId));
            }

            return tables;
        }

        /// <summary>
        /// The purpose of this function is to generate Table for a given table id in the provided
        /// stach data through the package.
        /// </summary>
        /// <param name="package"></param>
        /// <param name="primaryTableId"></param>
        /// <returns>Returns the generated Table from the package provided.</returns>
        private static Models.Table GenerateTable(Package package, string primaryTableId)
        {
            var primaryTable = package.Tables[primaryTableId];
            var headerId = primaryTable.Definition.HeaderTableId;
            var headerTable = package.Tables[headerId];
            var columnIds = primaryTable.Definition.Columns.Select(c => c.Id).ToList();
            var rowCount = primaryTable.Data.Rows.Count;

            var table = new Models.Table
            {
                Rows = new List<Row>(),
                Metadata = new Dictionary<string, string>()
            };

            var headerDataRowList = headerTable.Data.Rows.Select(x => x.Id).ToList();
            foreach (var headerTableSeriesDefinition in headerTable.Definition.Columns)
            {
                var headerRow = new Row {Cells = new List<string>()};
                foreach (var primaryTableSeriesDefinition in primaryTable.Definition.Columns)
                {
                    if (primaryTableSeriesDefinition.IsDimension)
                    {
                        var description = string.IsNullOrEmpty(primaryTableSeriesDefinition.Description)
                            ? primaryTableSeriesDefinition.Name
                            : primaryTableSeriesDefinition.Description;

                        headerRow.Cells.Add(description);
                        continue;
                    }

                    var index = headerDataRowList.IndexOf(primaryTableSeriesDefinition.HeaderId);
                    var value = headerTable.Data.Columns[headerTableSeriesDefinition.Id]
                        .GetValueHelper(headerTableSeriesDefinition.Type, index);
                    headerRow.Cells.Add(value.ToString());
                }

                headerRow.isHeader = true;
                table.Rows.Add(headerRow);
            }

            // Constructs the column data
            for (int i = 0; i < rowCount; i++)
            {
                var dataRow = new Row {Cells = new List<string>()};
                foreach (var columnId in columnIds)
                {
                    dataRow.Cells.Add(Convert.ToString(primaryTable.Data.Columns[columnId]
                        .GetValueHelper(primaryTable.Definition.Columns.First(c => c.Id == columnId).Type, i)));
                }

                table.Rows.Add(dataRow);
            }

            var metadataItems = primaryTable.Data.Metadata.Items;
            var tableMetadataLocations = primaryTable.Data.Metadata.Locations.Table;

            foreach (var location in tableMetadataLocations)
            {
                table.Metadata.Add(metadataItems[location].Name, metadataItems[location].StringValue);
            }

            return table;
        }
    }
}