package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.extensions.StachExtensionBuilder;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.models.DataAndMetaModel;
import com.factset.protobuf.stach.v2.PackageProto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        Gson gson = new GsonBuilder().create();
        DataAndMetaModel dataAndMetaModel = gson.fromJson(pkgString, DataAndMetaModel.class);

        if (dataAndMetaModel.data != null) {
            pkgString = gson.toJson(dataAndMetaModel.data);//dataAndMetaModel.data.toString();
        } else {
            pkgString = gson.toJson(gson.fromJson(pkgString, Object.class));
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
