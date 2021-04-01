package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.v2.ColumnOrganizedStachExtension;
import com.factset.protobuf.stach.extensions.v2.RowOrganizedStachExtension;

public class ExtensionFactory {

    public static StachExtensions getStachExtension(StachVersion version, String type) {

        if(version == StachVersion.V1){
            return new com.factset.protobuf.stach.extensions.v1.ColumnOrganizedStachExtension();
        }

        switch (type) {
            case "column":
                return new ColumnOrganizedStachExtension();
            case "row":
                return new RowOrganizedStachExtension();
            default:
                return null;
        }
    }

}
