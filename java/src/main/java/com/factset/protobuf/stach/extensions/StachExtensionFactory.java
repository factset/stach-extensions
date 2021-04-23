package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.models.StachType;
import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.v2.RowOrganizedStachBuilder;


public class StachExtensionFactory {

    public static StachExtensionBuilder getBuilder(StachVersion version, StachType stachType) {

        if (version == StachVersion.V1) {
            return new com.factset.protobuf.stach.extensions.v1.ColumnOrganizedStachBuilder();
        }

        switch (stachType) {
            case ColumnOrganized:
                return new com.factset.protobuf.stach.extensions.v2.ColumnOrganizedStachBuilder();
            case RowOrganized:
                return new RowOrganizedStachBuilder();
            default:
                return null;
        }
    }

}
