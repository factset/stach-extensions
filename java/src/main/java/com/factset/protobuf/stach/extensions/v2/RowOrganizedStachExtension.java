package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.models.Row;
import com.factset.protobuf.stach.extensions.models.TableData;
import com.factset.protobuf.stach.v2.RowOrganizedProto;
import com.factset.protobuf.stach.v2.table.ColumnDefinitionProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RowOrganizedStachExtension implements StachExtensions {

    private final RowOrganizedProto.RowOrganizedPackage pkg;

    public RowOrganizedStachExtension(RowOrganizedProto.RowOrganizedPackage pkg) {
        this.pkg = pkg;
    }

    /**
     * The purpose of this function is to convert row organized stach to Tabular format.
     *
     * @return Returns a list of tables for a given stach data.
     * @throws InvalidProtocolBufferException 
     */
    @Override
    public List<TableData> convertToTable() throws InvalidProtocolBufferException {
        List<TableData> tables = new ArrayList<TableData>();
        for (String tableKey : pkg.getTablesMap().keySet()) {
            tables.add(generateTable(pkg.getTablesMap().get(tableKey)));
        }
        return tables;
    }

    /**
     * The purpose of this function is to generate Table for a given RowOrganized stach table.
     *
     * @param stachTable : RowOrganized stach table object.
     * @return Returns the generated Table from the RowOrganized Table provided.
     * @throws InvalidProtocolBufferException 
     */
    private TableData generateTable(RowOrganizedProto.RowOrganizedPackage.Table stachTable) throws InvalidProtocolBufferException {
        TableData table = new TableData();

        int rowIndex = 0;
        RowOrganizedProto.RowOrganizedPackage.Row firstRow = stachTable.getData().getRows(rowIndex);

        if (firstRow.getRowType() != RowOrganizedProto.RowOrganizedPackage.Row.RowType.Header) {

            // if we dont have header row in the firstRow, it is simplifiedrow format
            // process simplifiedrow headers from description section
            Row headerRow = new Row();

            for (ColumnDefinitionProto.ColumnDefinition columnDefinition : stachTable.getDefinition().getColumnsList()) {
                String description = columnDefinition.getDescription() == null ? columnDefinition.getName() : columnDefinition.getDescription();
                headerRow.getCells().add(description);
            }
            headerRow.setHeader(true);
            table.getRows().add(headerRow);
        }


        // process header rows
        for (; rowIndex < stachTable.getData().getRowsCount(); rowIndex++) {

            Row headerRow = new Row();
            RowOrganizedProto.RowOrganizedPackage.Row currentRow = stachTable.getData().getRows(rowIndex);

            if (currentRow.getRowType() == RowOrganizedProto.RowOrganizedPackage.Row.RowType.Header) {

                for (Value val : currentRow.getCells().getValuesList()) {
                    Object valObj = StachUtilities.valueToObject(val);
                    headerRow.getCells().add(valObj == null ? "" : valObj.toString());
                }

                headerRow.setHeader(true);
                table.getRows().add(headerRow);

            } else {
                break;
            }
        }

        // process data rows
        // assuming we will have data rows as values
        for (; rowIndex < stachTable.getData().getRowsCount(); rowIndex++) {

            Row dataRow = new Row();
            Map<String, Value> rowDataMap = stachTable.getData().getRows(rowIndex).getValues().getFieldsMap();

            // Loop for each of column definition and find the key and add it or else null
            for (ColumnDefinitionProto.ColumnDefinition colDefinition : stachTable.getDefinition().getColumnsList()) {

                Object value = StachUtilities.valueToObject(rowDataMap.get(colDefinition.getId()));
                String valString = value == null ? colDefinition.getFormat().getNullFormat() : value.toString();
                dataRow.getCells().add(valString);
            }

            table.getRows().add(dataRow);
        }

        //process metadata
        for (String key : stachTable.getData().getTableMetadataMap().keySet()) {
            Object metaDataValue = StachUtilities.valueToObject(stachTable.getData().getTableMetadataMap().get(key).getValue());
            table.getMetadata().put(key, metaDataValue.toString());
        }

        return table;

    }

}
