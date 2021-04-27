package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.StachExtensionBuilderRow;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.v2.RowOrganizedProto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.util.HashMap;
import java.util.Map;

public class RowOrganizedStachBuilder implements StachExtensionBuilderRow {

    private RowOrganizedProto.RowOrganizedPackage rowOrgPackage;
    private Map<String, RowOrganizedProto.RowOrganizedPackage.Table> tableList = new HashMap<>();

    @Override
    public StachExtensionBuilderRow setPackage(RowOrganizedProto.RowOrganizedPackage pkg) {
        this.rowOrgPackage = pkg;
        return this;
    }

    @Override
    public StachExtensionBuilderRow setPackage(String pkgString) throws InvalidProtocolBufferException {

        RowOrganizedProto.RowOrganizedPackage.Builder builder = RowOrganizedProto.RowOrganizedPackage.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(pkgString, builder);
        this.rowOrgPackage = builder.build();
        return this;
    }

    @Override
    public StachExtensionBuilderRow setPackage(Object pkgObject) throws JsonProcessingException, InvalidProtocolBufferException {

        ObjectMapper mapper = new ObjectMapper();
        String pkgString = mapper.writeValueAsString(pkgObject);
        return setPackage(pkgString);
    }

    @Override
    public StachExtensionBuilderRow addTable(String tableId, RowOrganizedProto.RowOrganizedPackage.Table table) {
        tableList.put(tableId, table);
        return this;
    }

    @Override
    public StachExtensionBuilderRow addTable(String tableId, String tableString) {
        RowOrganizedProto.RowOrganizedPackage.Table.Builder builder = RowOrganizedProto.RowOrganizedPackage.Table.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(tableString, builder);
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Error while deserializing the response");
            e.printStackTrace();
        }

        tableList.put(tableId, builder.build());
        return this;
    }

    @Override
    public StachExtensionBuilderRow addTable(String tableId, Object tableObject) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String tableString = mapper.writeValueAsString(tableObject);
        return addTable(tableId, tableString);
    }

    @Override
    public StachExtensions build() {

        if(tableList != null && !tableList.isEmpty()){

            RowOrganizedProto.RowOrganizedPackage.Builder rowOrgPackageBuilder = RowOrganizedProto.RowOrganizedPackage.newBuilder();

            if(rowOrgPackage != null){
                for (String key:rowOrgPackage.getTablesMap().keySet()){
                    rowOrgPackageBuilder.putTables(key, rowOrgPackage.getTablesMap().get(key));
                }
            }

            for (Map.Entry<String, RowOrganizedProto.RowOrganizedPackage.Table> entry : tableList.entrySet()){
                rowOrgPackageBuilder.putTables(entry.getKey(), entry.getValue());
            }

            rowOrgPackage = rowOrgPackageBuilder.build();
        }
        return new RowOrganizedStachExtension(rowOrgPackage);
    }
}
