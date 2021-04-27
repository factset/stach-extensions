package com.factset.protobuf.stach.extensions.v1;

import com.factset.protobuf.stach.PackageProto;
import com.factset.protobuf.stach.extensions.StachExtensionBuilderColumn;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class ColumnOrganizedStachBuilder implements StachExtensionBuilderColumn<PackageProto.Package> {

    private PackageProto.Package pkg;

    @Override
    public StachExtensionBuilderColumn setPackage(PackageProto.Package pkg) {
        this.pkg = pkg;
        return this;
    }

    @Override
    public StachExtensionBuilderColumn setPackage(Object pkgObject) throws JsonProcessingException, InvalidProtocolBufferException {
        ObjectMapper mapper = new ObjectMapper();
        String pkgString = mapper.writeValueAsString(pkgObject);
        return setPackage(pkgString);
    }

    @Override
    public StachExtensionBuilderColumn setPackage(String pkgString) throws InvalidProtocolBufferException {

        PackageProto.Package.Builder builder = PackageProto.Package.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(pkgString, builder);
        this.pkg = builder.build();
        return this;
    }

    public ColumnOrganizedStachExtension build() {
        return new ColumnOrganizedStachExtension(pkg);
    }
}
