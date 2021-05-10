package com.factset.protobuf.stach.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

public interface ColumnStachExtensionBuilder<T extends GeneratedMessageV3> {

    /**
     * Sets the Package object.
     * @param pkg package object of type Package
     * @return builder instance
     */
    ColumnStachExtensionBuilder setPackage(T pkg);

    /**
     * Sets the Package object by parsing raw object input.
     * @param pkg package object
     * @return builder instance
     * @throws JsonProcessingException
     * @throws InvalidProtocolBufferException
     */
    ColumnStachExtensionBuilder setPackage(Object pkg) throws JsonProcessingException, InvalidProtocolBufferException;

    /**
     * Set the Package object by parsing the input in string format.
     * @param pkgString string form of package object
     * @return builder instance
     * @throws InvalidProtocolBufferException
     */
    ColumnStachExtensionBuilder setPackage(String pkgString) throws InvalidProtocolBufferException;

    /**
     * Builds the stach extension and returns the instance.
     * @return instance of column organized stach extension class.
     */
    StachExtensions build();
}
