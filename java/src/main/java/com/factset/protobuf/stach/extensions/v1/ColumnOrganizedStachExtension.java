package com.factset.protobuf.stach.extensions.v1;

import com.factset.protobuf.stach.MetadataItemProto.MetadataItem;
import com.factset.protobuf.stach.PackageProto.Package;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.Utilities;
import com.factset.protobuf.stach.extensions.models.Row;
import com.factset.protobuf.stach.extensions.models.TableData;
import com.factset.protobuf.stach.table.SeriesDataProto.SeriesData;
import com.factset.protobuf.stach.table.SeriesDefinitionProto;
import com.factset.protobuf.stach.table.SeriesDefinitionProto.SeriesDefinition;
import com.factset.protobuf.stach.table.TableProto.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColumnOrganizedStachExtension implements StachExtensions {

    private final Package pkg;

    public ColumnOrganizedStachExtension(Package pkg) {
        this.pkg = pkg;
    }

    /**
     * The purpose of this function is to generate Table for a given table id in the provided stach data through the package.
     *
     * @param pkg     : Stach Data which is represented as a Package object.
     * @param primaryTableId : Refers to the id for a particular table inside a package.
     * @return Returns the generated Table from the package provided.
     */
    private static TableData generateTable(Package pkg, String primaryTableId) {
        Map<String, Table> tablesMap = pkg.getTablesMap();
        Table primaryTable = tablesMap.get(primaryTableId);
        String headerId = primaryTable.getDefinition().getHeaderTableId();
        Table headerTable = tablesMap.get(headerId);
        int rowsCount = primaryTable.getData().getRowsCount();

        TableData table = new TableData();

        List<SeriesDefinition> headerTableSeriesDefinitions = headerTable.getDefinition().getColumnsList();
        List<SeriesDefinition> primaryTableSeriesDefinitions = primaryTable.getDefinition().getColumnsList();

        Map<String, SeriesData> headerTableColumns = headerTable.getData().getColumnsMap();
        Map<String, SeriesData> primaryTableColumns = primaryTable.getData().getColumnsMap();

        for (SeriesDefinition headerTableseriesDefinition : headerTableSeriesDefinitions) {
            Row headerRow = new Row();
            headerRow.setHeader(true);

            for (SeriesDefinition primaryTableSeriesDefinition : primaryTableSeriesDefinitions) {
                if (primaryTableSeriesDefinition.getIsDimension()) {
                    headerRow.getCells().add(Utilities.isNullOrEmpty(primaryTableSeriesDefinition.getDescription()) ?
                            primaryTableSeriesDefinition.getName() : primaryTableSeriesDefinition.getDescription());
                    continue;
                }

                String headerColumnId = primaryTableSeriesDefinition.getHeaderId();
                String nullFormat = headerTableseriesDefinition.getFormat().getNullFormat();

                int indexOfHeader = StachUtilities.getIndexOf(headerTable.getData().getRowsList(), headerColumnId);
                headerRow.getCells().add(StachUtilities.getValueHelper(headerTableColumns.get(headerTableseriesDefinition.getId()),
                        headerTableseriesDefinition.getType(), indexOfHeader, nullFormat).toString());
            }

            table.getRows().add(headerRow);
        }

        // Construct the column data
        for (int i = 0; i < rowsCount; i++) {
            Row dataRow = new Row();
            for (SeriesDefinitionProto.SeriesDefinition primaryTableSeriesDefinition : primaryTableSeriesDefinitions) {
                String nullFormat = primaryTableSeriesDefinition.getFormat().getNullFormat();
                String primaryTableColumnId = primaryTableSeriesDefinition.getId();
                dataRow.getCells().add(StachUtilities.getValueHelper(primaryTableColumns.get(primaryTableColumnId),
                        primaryTableSeriesDefinition.getType(), i, nullFormat).toString());
            }
            table.getRows().add(dataRow);
        }

        Map<String, MetadataItem> metadata = primaryTable.getData().getMetadata().getItemsMap();
        for (Map.Entry<String, MetadataItem> entry : metadata.entrySet()) {
            table.getMetadata().put(entry.getValue().getName(), entry.getValue().getStringValue());
        }
        return table;
    }

    /**
     * The purpose of this function is to convert stach to Tabular format.
     *
     * @return Returns a list of tables for a given stach data.
     */
    @Override
    public List<TableData> convertToTable() {
        List<TableData> tables = new ArrayList<>();
        for (String primaryTableId : pkg.getPrimaryTableIdsList()) {
            tables.add(generateTable(pkg, primaryTableId));
        }
        return tables;
    }

}