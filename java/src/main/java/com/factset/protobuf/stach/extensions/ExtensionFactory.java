package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.models.Stach2Type;
import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.v2.ColumnOrganizedStachExtension;
import com.factset.protobuf.stach.extensions.v2.RowOrganizedStachExtension;

public class ExtensionFactory {

    public static StachExtensions getStachExtension(StachVersion version, Stach2Type stach2Type) {

        if(version == StachVersion.V1){
            return new com.factset.protobuf.stach.extensions.v1.ColumnOrganizedStachExtension();
        }

        switch (stach2Type) {
            case ColumnOrganized:
                return new ColumnOrganizedStachExtension();
            case RowOrganized:
                return new RowOrganizedStachExtension();
            default:
                return null;
        }
    }

}
