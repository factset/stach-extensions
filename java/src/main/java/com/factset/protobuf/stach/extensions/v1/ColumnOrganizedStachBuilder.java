package com.factset.protobuf.stach.extensions.v1;

import com.factset.protobuf.stach.PackageProto;
import com.factset.protobuf.stach.extensions.ColumnStachExtensionBuilder;
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
    public PackageProto.Package getPackage() {
        return this.pkg;
    }

    public ColumnOrganizedStachExtension build() {
        return new ColumnOrganizedStachExtension(pkg);
    }
}
