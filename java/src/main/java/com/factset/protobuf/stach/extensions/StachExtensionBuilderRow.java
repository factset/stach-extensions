package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.v2.RowOrganizedProto;
import com.fasterxml.jackson.core.JsonProcessingException;


public interface StachExtensionBuilderRow {

    /**
     * Sets the RowOrganizedPackage object.
     * @param pkg package object of type RowOrganizedPackage
     * @return builder instance
     */
    StachExtensionBuilderRow setPackage(RowOrganizedProto.RowOrganizedPackage pkg);

    /**
     * Sets the RowOrganizedPackage object by parsing raw object input.
     * @param pkg package object
     * @return builder instance
     */
    StachExtensionBuilderRow setPackage(Object pkg) throws JsonProcessingException;

    /**
     * Set the RowOrganizedPackage object by parsing the input in string format.
     * @param pkgString string form of package object
     * @return builder instance
     */
    StachExtensionBuilderRow setPackage(String pkgString);

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

    /**
     * Builds the stach extension and returns the instance.
     * @return instance of Column or Row organized stach extension class.
     */
    StachExtensions build();
}
