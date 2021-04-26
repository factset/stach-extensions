package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.v2.RowOrganizedProto;
import com.fasterxml.jackson.core.JsonProcessingException;


public interface StachExtensionBuilderRow extends StachExtensionBuilder<RowOrganizedProto.RowOrganizedPackage> {

    /**
     * Add the Row Organized Table to the package
     * @param tableId id of the table
     * @param table Row Organized Table object
     * @return Builder instance
     */
    StachExtensionBuilderRow addTable(String tableId, RowOrganizedProto.RowOrganizedPackage.Table table);

    /**
     * Add the Row Organized Table to the package
     * @param tableId id of the table
     * @param tableString Row Organized Table in string format
     * @return Builder instance
     */
    StachExtensionBuilderRow addTable(String tableId, String tableString);

    /**
     * Add the Row Organized Table to the package
     * @param tableId id of the table
     * @param tableObject Row organized table object
     * @return Builder instance
     */
    StachExtensionBuilderRow addTable(String tableId, Object tableObject) throws JsonProcessingException;

}
