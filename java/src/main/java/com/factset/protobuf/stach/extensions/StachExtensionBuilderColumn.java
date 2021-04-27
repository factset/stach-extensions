package com.factset.protobuf.stach.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.GeneratedMessageV3;

public interface StachExtensionBuilderColumn<T extends GeneratedMessageV3> {

    /**
     * Sets the Package object.
     * @param pkg package object of type Package/RowOrganizedPackage
     * @return builder instance
     */
    StachExtensionBuilderColumn setPackage(T pkg);

    /**
     * Sets the Package object by parsing raw object input.
     * @param pkg package object
     * @return builder instance
     */
    StachExtensionBuilderColumn setPackage(Object pkg) throws JsonProcessingException;

    /**
     * Set the Package object by parsing the input in string format.
     * @param pkgString string form of package object
     * @return builder instance
     */
    StachExtensionBuilderColumn setPackage(String pkgString);

    /**
     * Builds the stach extension and returns the instance.
     * @return instance of Column or Row organized stach extension class.
     */
    StachExtensions build();
}
