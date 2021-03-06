package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.Utilities;
import com.factset.protobuf.stach.extensions.models.Row;
import com.factset.protobuf.stach.extensions.models.TableData;
import com.factset.protobuf.stach.v2.PackageProto;
import com.factset.protobuf.stach.v2.table.ColumnDataProto;
import com.factset.protobuf.stach.v2.table.ColumnDefinitionProto;
import com.factset.protobuf.stach.v2.table.MetadataItemProto;
import com.factset.protobuf.stach.v2.table.TableProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ListValue;
import com.google.protobuf.Value;

import java.util.*;

public class ColumnOrganizedStachExtension implements StachExtensions {

    PackageProto.Package pkg;

    public ColumnOrganizedStachExtension(PackageProto.Package pkg) {
        this.pkg = pkg;
    }

    /**
     * The purpose of this function is to convert stach to Tabular format.
     *
     * @return Returns a list of tables for a given stach data.
     * @throws InvalidProtocolBufferException 
     */
    @Override
    public List<TableData> convertToTable() throws InvalidProtocolBufferException {
        List<TableData> tables = new ArrayList<>();
        for (String primaryTableId : pkg.getPrimaryTableIdsList()) {
            tables.add(generateTable(pkg, primaryTableId));
        }
        return tables;
    }

    /**
     * The purpose of this function is to generate Table for a given table id in the provided stach data through the package.
     *
     * @param packageObj     : Stach Data which is represented as a Package object.
     * @param primaryTableId : Refers to the id for a particular table inside a package.
     * @return Returns the generated Table from the package provided.
     * @throws InvalidProtocolBufferException 
     */
    private TableData generateTable(PackageProto.Package packageObj, String primaryTableId) throws InvalidProtocolBufferException {
        Map<String, TableProto.Table> tablesMap = packageObj.getTablesMap();
        TableProto.Table primaryTable = tablesMap.get(primaryTableId);
        String headerId = primaryTable.getDefinition().getHeaderTableId();
        int rowsCount = primaryTable.getData().getRowsCount();

        TableData table = new TableData();

        List<ColumnDefinitionProto.ColumnDefinition> primaryTableSeriesDefinitions = primaryTable.getDefinition().getColumnsList();
        Map<String, ColumnDataProto.ColumnData> primaryTableColumns = primaryTable.getData().getColumnsMap();

        if (headerId.length() > 0) {
            TableProto.Table headerTable = tablesMap.get(headerId);
            // Construct the column headers by considering dimension columns and header
            // rows.
            List<ColumnDefinitionProto.ColumnDefinition> headerTableSeriesDefinitions = headerTable.getDefinition().getColumnsList();
            Map<String, ColumnDataProto.ColumnData> headerTableColumns = headerTable.getData().getColumnsMap();

            for (ColumnDefinitionProto.ColumnDefinition headerTableseriesDefinition : headerTableSeriesDefinitions) {
                Row headerRow = new Row();
                headerRow.setHeader(true);
                for (ColumnDefinitionProto.ColumnDefinition primaryTableSeriesDefinition : primaryTableSeriesDefinitions) {
                    if (primaryTableSeriesDefinition.getIsDimension()) {
                        headerRow.getCells().add(Utilities.isNullOrEmpty(primaryTableSeriesDefinition.getDescription()) ? primaryTableSeriesDefinition.getName() : primaryTableSeriesDefinition.getDescription());
                        continue;
                    }

                    String headerColumnId = primaryTableSeriesDefinition.getHeaderId();
                    String nullFormat = headerTableseriesDefinition.getFormat().getNullFormat();

                    int indexOfHeader = StachUtilities.getIndexOf(headerTable.getData().getRowsList(), headerColumnId);
                    Value val = headerTableColumns.get(headerTableseriesDefinition.getId()).getValues().getValues(indexOfHeader);
                    String valObj = StachUtilities.valueToString(val);
                    headerRow.getCells().add(valObj == null ? nullFormat : valObj);
                }
                table.getRows().add(headerRow);
            }
        }
        else {
            // if there is no headers table process headers from description section
            Row headerRow = new Row();

            for (ColumnDefinitionProto.ColumnDefinition columnDefinition : primaryTable.getDefinition().getColumnsList()) {
                String description = Utilities.isNullOrEmpty(columnDefinition.getDescription()) ? columnDefinition.getName() : columnDefinition.getDescription();
                headerRow.getCells().add(description);
            }
            headerRow.setHeader(true);
            table.getRows().add(headerRow);
        }

        // Construct the column data
        for (int i = 0; i < rowsCount; i++) {
            Row dataRow = new Row();
            for (ColumnDefinitionProto.ColumnDefinition primaryTableSeriesDefinition : primaryTableSeriesDefinitions) {
                String nullFormat = primaryTableSeriesDefinition.getFormat().getNullFormat();

                String primaryTableColumnId = primaryTableSeriesDefinition.getId();
                Value val = primaryTableColumns.get(primaryTableColumnId).getValues().getValues(i);
                String valObj = StachUtilities.valueToString(val);

                dataRow.getCells().add(valObj == null ? nullFormat : valObj);
            }
            table.getRows().add(dataRow);
        }

        Map<String, MetadataItemProto.MetadataItem> metadata = primaryTable.getData().getMetadata().getItemsMap();
        for (Map.Entry<String, MetadataItemProto.MetadataItem> entry : metadata.entrySet()) {
            table.getMetadata().put(entry.getKey(), StachUtilities.valueToString(entry.getValue().getValue()));

            Value metadataValue = entry.getValue().getValue();
            if (metadataValue.getKindCase() == Value.KindCase.LIST_VALUE) {
                List<Value> valuesList = metadataValue.getListValue().getValuesList();
                table.getRawMetadata().put(entry.getKey(), valuesList);
            }
            else {
                List<Value> valuesList = Arrays.asList(metadataValue);
                table.getRawMetadata().put(entry.getKey(), valuesList);
            }
        }

        return table;
    }

}


