package com.factset.protobuf.stach.extensions;

public interface StachExtensionBuilder<T> {

    StachExtensionBuilder set(T pkg);

    StachExtensionBuilder set(String pkgString);

    StachExtensions build();
}
