package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.StachUtilities;
import com.factset.protobuf.stach.v2.PackageProto;
import com.factset.protobuf.stach.v2.RowOrganizedProto;
import com.factset.protobuf.stach.v2.table.ColumnDefinitionProto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import com.factset.protobuf.stach.extensions.models.DataAndMetaModel;
import com.factset.protobuf.stach.extensions.models.Row;
import com.factset.protobuf.stach.extensions.models.TableData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RowOrganizedStachExtension extends StachUtilities implements StachExtensions<RowOrganizedProto.RowOrganizedPackage> {

    private RowOrganizedProto.RowOrganizedPackage _package;

    public RowOrganizedStachExtension() {
    }

    public RowOrganizedStachExtension(RowOrganizedProto.RowOrganizedPackage _package) {
        this._package = _package;
    }

    public RowOrganizedStachExtension(String jsonString) {
        this._package = parseString(jsonString);
    }

    @Override
    public RowOrganizedProto.RowOrganizedPackage parseString(String jsonString) {

        Gson gson = new GsonBuilder().create();
        DataAndMetaModel dataAndMetaModel = gson.fromJson(jsonString, DataAndMetaModel.class);

        if (dataAndMetaModel.data != null) {
            jsonString = gson.toJson(dataAndMetaModel.data);
        } else {
            jsonString = gson.toJson(gson.fromJson(jsonString, Object.class));
        }

        RowOrganizedProto.RowOrganizedPackage.Builder builder = RowOrganizedProto.RowOrganizedPackage.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(jsonString, builder);
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Error while deserializing the response");
            e.printStackTrace();
        }

        return builder.build();
    }

    @Override
    public List<TableData> convertToTable(RowOrganizedProto.RowOrganizedPackage _package) {
        List<TableData> tables = new ArrayList<TableData>();
        for (String tableKey : _package.getTablesMap().keySet()) {
            tables.add(generateTable(_package.getTablesMap().get(tableKey)));
        }
        return tables;
    }

    @Override
    public List<TableData> convertToTable(String pkgString) {
        RowOrganizedProto.RowOrganizedPackage _package = parseString(pkgString);
        return convertToTable(_package);
    }

    private TableData generateTable(RowOrganizedProto.RowOrganizedPackage.Table stachTable) {
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
                    Object valObj = valueToObject(val);
                    headerRow.getCells().add(valObj == null ? "" : valObj.toString()); //TODO - replace empty string with null format for null case
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

                String value = rowDataMap.get(colDefinition.getId()) == null ? "" : valueToObject(rowDataMap.get(colDefinition.getId())).toString();
                dataRow.getCells().add(value);
            }

            table.getRows().add(dataRow);
        }

        return table;

    }

}
