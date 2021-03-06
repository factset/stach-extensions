package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.RowStachExtensionBuilder;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.v2.RowOrganizedProto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.util.HashMap;
import java.util.Map;

public class RowOrganizedStachBuilder implements RowStachExtensionBuilder {

    private RowOrganizedProto.RowOrganizedPackage rowOrgPackage;
    private Map<String, RowOrganizedProto.RowOrganizedPackage.Table> tableList = new HashMap<>();

    @Override
    public RowStachExtensionBuilder setPackage(RowOrganizedProto.RowOrganizedPackage pkg) {
        this.rowOrgPackage = pkg;
        return this;
    }

    @Override
    public RowStachExtensionBuilder setPackage(String pkgString) throws InvalidProtocolBufferException {

        RowOrganizedProto.RowOrganizedPackage.Builder builder = RowOrganizedProto.RowOrganizedPackage.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(pkgString, builder);
        this.rowOrgPackage = builder.build();
        return this;
    }

    @Override
    public RowStachExtensionBuilder setPackage(Object pkgObject) throws JsonProcessingException, InvalidProtocolBufferException {

        ObjectMapper mapper = new ObjectMapper();
        String pkgString = mapper.writeValueAsString(pkgObject);
        return setPackage(pkgString);
    }

    @Override
    public RowStachExtensionBuilder addTable(String tableId, RowOrganizedProto.RowOrganizedPackage.Table table) {
        tableList.put(tableId, table);
        return this;
    }

    @Override
    public RowStachExtensionBuilder addTable(String tableId, String tableString) throws InvalidProtocolBufferException {
        RowOrganizedProto.RowOrganizedPackage.Table.Builder builder = RowOrganizedProto.RowOrganizedPackage.Table.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(tableString, builder);
        tableList.put(tableId, builder.build());
        return this;
    }

    @Override
    public RowStachExtensionBuilder addTable(String tableId, Object tableObject) throws JsonProcessingException, InvalidProtocolBufferException {
        ObjectMapper mapper = new ObjectMapper();
        String tableString = mapper.writeValueAsString(tableObject);
        return addTable(tableId, tableString);
    }

    @Override
    public RowOrganizedProto.RowOrganizedPackage getPackage() {
        return this.rowOrgPackage;
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
