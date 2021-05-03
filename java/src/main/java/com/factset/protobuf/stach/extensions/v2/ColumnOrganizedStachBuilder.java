package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.ColumnStachExtensionBuilder;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.v2.PackageProto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class ColumnOrganizedStachBuilder implements ColumnStachExtensionBuilder<PackageProto.Package> {

    private PackageProto.Package pkg;

    @Override
    public ColumnStachExtensionBuilder setPackage(PackageProto.Package pkg) {
        this.pkg = pkg;
        return this;
    }

    @Override
    public ColumnStachExtensionBuilder setPackage(Object pkgObject) throws JsonProcessingException, InvalidProtocolBufferException {
        ObjectMapper mapper = new ObjectMapper();
        String pkgString = mapper.writeValueAsString(pkgObject);
        return setPackage(pkgString);
    }

    @Override
    public ColumnStachExtensionBuilder setPackage(String pkgString) throws InvalidProtocolBufferException {

        PackageProto.Package.Builder builder = PackageProto.Package.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(pkgString, builder);
        this.pkg = builder.build();
        return this;
    }


    @Override
    public StachExtensions build() {
        return new ColumnOrganizedStachExtension(pkg);
    }
}
