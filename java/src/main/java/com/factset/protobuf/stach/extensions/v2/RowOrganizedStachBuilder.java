package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.PackageProto;
import com.factset.protobuf.stach.extensions.StachExtensionBuilder;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.models.DataAndMetaModel;
import com.factset.protobuf.stach.v2.RowOrganizedProto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class RowOrganizedStachBuilder implements StachExtensionBuilder<RowOrganizedProto.RowOrganizedPackage> {

    private RowOrganizedProto.RowOrganizedPackage rowOrgPackage;

    @Override
    public StachExtensionBuilder set(RowOrganizedProto.RowOrganizedPackage pkg) {
        this.rowOrgPackage = pkg;
        return this;
    }

    @Override
    public StachExtensionBuilder set(String pkgString) {
        Gson gson = new GsonBuilder().create();
        DataAndMetaModel dataAndMetaModel = gson.fromJson(pkgString, DataAndMetaModel.class);

        if (dataAndMetaModel.data != null) {
            pkgString = gson.toJson(dataAndMetaModel.data);
        } else {
            pkgString = gson.toJson(gson.fromJson(pkgString, Object.class));
        }

        RowOrganizedProto.RowOrganizedPackage.Builder builder = RowOrganizedProto.RowOrganizedPackage.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(pkgString, builder);
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Error while deserializing the response");
            e.printStackTrace();
        }

        this.rowOrgPackage = builder.build();
        return this;
    }

    @Override
    public StachExtensions build() {
        return new RowOrganizedStachExtension(rowOrgPackage);
    }
}
