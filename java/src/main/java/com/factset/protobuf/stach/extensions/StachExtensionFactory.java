package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.v1.ColumnOrganizedStachBuilder;
import com.factset.protobuf.stach.extensions.v2.RowOrganizedStachBuilder;


public class StachExtensionFactory {

    /**
     * Returns the appropriate column organized builder based on the stach version provided.
     * @param version The stach version.
     * @return version specific column organized stach builder.
     */
    public static ColumnStachExtensionBuilder getColumnOrganizedBuilder(StachVersion version) {

        switch (version) {
            case V1:
                return new ColumnOrganizedStachBuilder();
            case V2:
                return new com.factset.protobuf.stach.extensions.v2.ColumnOrganizedStachBuilder();
            default:
                return null;
        }
    }

    /**
     * Returns the row organized stach builder.
     * @return row organized stach builder.
     */
    public static RowStachExtensionBuilder getRowOrganizedBuilder(StachVersion version) {

        switch (version) {
            case V1:
                throw new UnsupportedOperationException("v1 version is not supported for row organized stach input");
            case V2:
                return new RowOrganizedStachBuilder();
            default:
                return null;
        }
    }
}
