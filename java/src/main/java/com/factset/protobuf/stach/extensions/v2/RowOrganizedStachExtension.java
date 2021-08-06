package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.Utilities;
import com.factset.protobuf.stach.extensions.models.Row;
import com.factset.protobuf.stach.extensions.models.RowSpanSpread;
import com.factset.protobuf.stach.extensions.models.TableData;
import com.factset.protobuf.stach.v2.RowOrganizedProto;
import com.factset.protobuf.stach.v2.table.ColumnDefinitionProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.Arrays;
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
                String description = Utilities.isNullOrEmpty(columnDefinition.getDescription()) ? columnDefinition.getName() : columnDefinition.getDescription();
                headerRow.getCells().add(description);
            }
            headerRow.setHeader(true);
            table.getRows().add(headerRow);
        }


        // Stores the info about the values that needs to be spread based on rowspan.
        List<RowSpanSpread> rowSpanSpreadList = new ArrayList<>();

        // process header rows
        for (; rowIndex < stachTable.getData().getRowsCount(); rowIndex++) {

            RowOrganizedProto.RowOrganizedPackage.Row currentRow = stachTable.getData().getRows(rowIndex);

            if (currentRow.getRowType() == RowOrganizedProto.RowOrganizedPackage.Row.RowType.Header) {

                Row headerRow = new Row();
                int index = 0;         // index pointing to the items in the current header row cells list.
                int position = 0;      // index pointing to the current column position (considering the rowspan and colspan)

                ArrayList<Value> headerRowValues =   new ArrayList<>(currentRow.getCells().getValuesList());
                ArrayList<RowOrganizedProto.RowOrganizedPackage.HeaderCellDetail>headerCellDetailsArray =
                        new ArrayList<>( currentRow.getHeaderCellDetailsMap().values());


                // Checking if any values needs to be added at the start of a header row, based on the
                // rowspread info from previous filled rows
                List<Value> values = StachUtilities.checkAddRowSpannedItem(position, rowIndex, rowSpanSpreadList);
                if(values != null){
                    position = position + values.size();
                    for(Value val : values){
                        String valObj = StachUtilities.valueToString(val);
                        headerRow.getCells().add(valObj == null ? "" : valObj);
                    }
                }

                for (Value val: headerRowValues) {

                    int colspan = headerCellDetailsArray.get(index).getColspan() <= 1 ? 1 : headerCellDetailsArray.get(index).getColspan();
                    int rowspan = headerCellDetailsArray.get(index).getRowspan() <= 1 ? 1 : headerCellDetailsArray.get(index).getRowspan();

                    if(rowspan > 1){
                        rowSpanSpreadList.add(new RowSpanSpread(position, rowspan, colspan, val));
                    }

                    String valObj = StachUtilities.valueToString(headerRowValues.get(index));
                    for(int i=0;i<colspan;i++){
                        headerRow.getCells().add(valObj == null ? "" : valObj);
                        position ++;

                        // After incrementing the position, checking and adding if any value needs to be inserted
                        // at the updated position based on the info in the rowspread list
                        values = StachUtilities.checkAddRowSpannedItem(position, rowIndex, rowSpanSpreadList);
                        if(values != null){
                            position = position + values.size();
                            for(Value spannedValue : values){
                                String spannedvalObj = StachUtilities.valueToString(spannedValue);
                                headerRow.getCells().add(spannedvalObj == null ? "" : spannedvalObj);
                            }
                        }
                    }

                    headerRow.setHeader(true);
                    index ++;
                }
                table.getRows().add(headerRow);
            } else {
                Row dataRow = new Row();
                Map<String, Value> rowDataMap = stachTable.getData().getRows(rowIndex).getValues().getFieldsMap();

                // Loop for each of column definition and find the key and add it or else null
                for (ColumnDefinitionProto.ColumnDefinition colDefinition : stachTable.getDefinition().getColumnsList()) {

                    String value = StachUtilities.valueToString(rowDataMap.get(colDefinition.getId()));
                    String valString = value == null ? colDefinition.getFormat().getNullFormat() : value;
                    dataRow.getCells().add(valString);
                }

                table.getRows().add(dataRow);
            }
        }


        //process metadata
        for (String key : stachTable.getData().getTableMetadataMap().keySet()) {
            String metaDataValue = StachUtilities.valueToString(stachTable.getData().getTableMetadataMap().get(key).getValue());
            table.getMetadata().put(key, metaDataValue);
        }

        return table;

    }

}
