package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.StachUtilities;
import com.factset.protobuf.stach.v2.PackageProto;
import com.factset.protobuf.stach.v2.table.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.factset.protobuf.stach.extensions.models.DataAndMetaModel;
import com.factset.protobuf.stach.extensions.models.Row;
import com.factset.protobuf.stach.extensions.models.TableData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColumnOrganizedStachExtension extends StachUtilities implements StachExtensions<PackageProto.Package> {

    PackageProto.Package _package;

    @Override
    public PackageProto.Package parseString(String jsonString) {

        Gson gson = new GsonBuilder().create();
        DataAndMetaModel dataAndMetaModel = gson.fromJson(jsonString, DataAndMetaModel.class);

        if (dataAndMetaModel.data != null) {
            jsonString = gson.toJson(dataAndMetaModel.data);//dataAndMetaModel.data.toString();
        } else {
            jsonString = gson.toJson(gson.fromJson(jsonString, Object.class));
        }

        PackageProto.Package.Builder builder = PackageProto.Package.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(jsonString, builder);
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Error while deserializing the response");
            e.printStackTrace();
        }

        return builder.build();
    }


    @Override
    public List<TableData> convertToTable(PackageProto.Package _package) {
        List<TableData> tables = new ArrayList<>();
        for (String primaryTableId : _package.getPrimaryTableIdsList()) {
            tables.add(generateTable(_package, primaryTableId));
        }
        return tables;
    }

    @Override
    public List<TableData> convertToTable(String pkgString) {
        PackageProto.Package _package = parseString(pkgString);
        return convertToTable(_package);
    }

    private  TableData generateTable(PackageProto.Package packageObj, String primaryTableId) {
        Map<String, TableProto.Table> tablesMap = packageObj.getTablesMap();
        TableProto.Table primaryTable = tablesMap.get(primaryTableId);
        String headerId = primaryTable.getDefinition().getHeaderTableId();
        TableProto.Table headerTable = tablesMap.get(headerId);
        int rowsCount = primaryTable.getData().getRowsCount();

        TableData table = new TableData();

        // Construct the column headers by considering dimension columns and header
        // rows.
        List<ColumnDefinitionProto.ColumnDefinition> headerTableSeriesDefinitions = headerTable.getDefinition().getColumnsList();
        List<ColumnDefinitionProto.ColumnDefinition> primaryTableSeriesDefinitions = primaryTable.getDefinition().getColumnsList();

        Map<String, ColumnDataProto.ColumnData> headerTableColumns = headerTable.getData().getColumnsMap();
        Map<String, ColumnDataProto.ColumnData> primaryTableColumns = primaryTable.getData().getColumnsMap();

        for (ColumnDefinitionProto.ColumnDefinition headerTableseriesDefinition : headerTableSeriesDefinitions) {
            Row headerRow = new Row();
            headerRow.setHeader(true);
            for (ColumnDefinitionProto.ColumnDefinition primaryTableSeriesDefinition : primaryTableSeriesDefinitions) {
                if (primaryTableSeriesDefinition.getIsDimension()) {
                    headerRow.getCells().add(isNullOrEmpty(primaryTableSeriesDefinition.getDescription()) ? primaryTableSeriesDefinition.getName() : primaryTableSeriesDefinition.getDescription());
                    continue;
                }

                String headerColumnId = primaryTableSeriesDefinition.getHeaderId();
                String nullFormat = headerTableseriesDefinition.getFormat().getNullFormat();

                int indexOfHeader = getIndexOf(headerTable.getData().getRowsList(), headerColumnId);
                Value val = headerTableColumns.get(headerTableseriesDefinition.getId()).getValues().getValues(indexOfHeader);
                Object valObj = valueToObject(val);
                headerRow.getCells().add(valObj == null ? nullFormat : valObj.toString());
            }
            table.getRows().add(headerRow);
        }
        // Construct the column data
        for (int i = 0; i < rowsCount; i++) {
            Row dataRow = new Row();
            for (ColumnDefinitionProto.ColumnDefinition primaryTableSeriesDefinition : primaryTableSeriesDefinitions) {
                String nullFormat = primaryTableSeriesDefinition.getFormat().getNullFormat();

                String primaryTableColumnId = primaryTableSeriesDefinition.getId();
                Value val = primaryTableColumns.get(primaryTableColumnId).getValues().getValues(i);
                Object valObj = valueToObject(val);

                dataRow.getCells().add(valObj == null ? nullFormat : valObj.toString());
            }
            table.getRows().add(dataRow);
        }

        Map<String, MetadataItemProto.MetadataItem> metadata = primaryTable.getData().getMetadata().getItemsMap();
        for (Map.Entry<String, MetadataItemProto.MetadataItem> entry : metadata.entrySet()) {
            table.getMetadata().put(entry.getKey(), entry.getValue().toString());
        }

        return table;
    }

}


