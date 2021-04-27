package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.v2.ColumnOrganizedStachBuilderColumn;
import com.factset.protobuf.stach.extensions.v2.RowOrganizedStachBuilder;


public class StachExtensionFactory {

    public static StachExtensionBuilderColumn getColumnOrganizedBuilder(StachVersion version) {

        switch (version) {
            case V1:
                return new com.factset.protobuf.stach.extensions.v1.ColumnOrganizedStachBuilderColumn();
            case V2:
                return new ColumnOrganizedStachBuilderColumn();
            default:
                return null;
        }
    }

    public static StachExtensionBuilderRow getRowOrganizedBuilder() {
        return new RowOrganizedStachBuilder();
    }
}
