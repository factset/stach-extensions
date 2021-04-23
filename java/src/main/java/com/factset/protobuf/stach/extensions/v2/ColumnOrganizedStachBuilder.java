package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.StachExtensionBuilder;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.models.DataAndMetaModel;
import com.factset.protobuf.stach.v2.PackageProto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class ColumnOrganizedStachBuilder implements StachExtensionBuilder<PackageProto.Package> {

    private PackageProto.Package pkg;

    @Override
    public StachExtensionBuilder set(PackageProto.Package pkg) {
        this.pkg = pkg;
        return this;
    }

    @Override
    public StachExtensionBuilder set(String pkgString) {
        DataAndMetaModel dataAndMetaModel = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            dataAndMetaModel = mapper.readValue(pkgString, DataAndMetaModel.class);
            if (dataAndMetaModel != null && dataAndMetaModel.data != null) {
                pkgString = mapper.writeValueAsString(dataAndMetaModel.data);
            }
        } catch (JsonMappingException e) {

        } catch (JsonProcessingException e) {

        }

        PackageProto.Package.Builder builder = PackageProto.Package.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(pkgString, builder);
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Error while deserializing the response");
            e.printStackTrace();
        }

        this.pkg = builder.build();
        return this;
    }

    @Override
    public StachExtensions build() {
        return new ColumnOrganizedStachExtension(pkg);
    }
}
