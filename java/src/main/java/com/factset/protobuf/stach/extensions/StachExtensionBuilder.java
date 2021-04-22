package com.factset.protobuf.stach.extensions;

public interface StachExtensionBuilder<T> {

    public StachExtensionBuilder set(T pkg);

    public StachExtensionBuilder set(String pkgString);

    public StachExtensions build();
}
