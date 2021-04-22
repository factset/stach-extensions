package com.factset.protobuf.stach.extensions.v1;

import com.factset.protobuf.stach.PackageProto;
import com.factset.protobuf.stach.extensions.StachExtensionBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class ColumnOrganizedStachBuilder implements StachExtensionBuilder<PackageProto.Package> {

    private PackageProto.Package pkg;

    public ColumnOrganizedStachBuilder set(PackageProto.Package pkg){
        this.pkg = pkg;
        return this;
    }

    public ColumnOrganizedStachBuilder set(String pkgString){
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

    public ColumnOrganizedStachExtension build(){
        return new ColumnOrganizedStachExtension(pkg);
    }
}
