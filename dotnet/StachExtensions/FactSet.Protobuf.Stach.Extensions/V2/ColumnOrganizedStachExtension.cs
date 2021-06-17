﻿using System;
using System.Collections.Generic;
using System.Linq;
using FactSet.Protobuf.Stach.Extensions.Models;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public class ColumnOrganizedStachExtension : IStachExtension
    {
        private readonly Stach.V2.Package pkg;
        
        public ColumnOrganizedStachExtension(Stach.V2.Package pkg)
        {
            this.pkg = pkg;
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
        private static Models.Table GenerateTable(Stach.V2.Package package, string primaryTableId)
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
                    var value = headerTable.Data.Columns[headerTableSeriesDefinition.Id].Values.Values[index];
                        
                    headerRow.Cells.Add(StachUtilities.ValueToString(value));
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
                    dataRow.Cells.Add(
                        StachUtilities.ValueToString(primaryTable.Data.Columns[columnId].Values.Values[i]) ?? string.Empty);
                }

                table.Rows.Add(dataRow);
            }

            var metadataItems = primaryTable.Data.Metadata.Items;
            var tableMetadataLocations = primaryTable.Data.Metadata.Locations.Table;

            foreach (var location in tableMetadataLocations)
            {
                metadataItems.TryGetValue(location, out var metadataItem);
                if (metadataItem != null)
                {
                    table.Metadata.Add(location, metadataItem.Value.StringValue);
                }
            }

            return table;
        }

    }
}