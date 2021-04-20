package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.models.TableData;

import java.util.List;

public interface StachExtensions<T> {

    public T convertToPackage(String jsonString);

    public List<TableData> convertToTable(T _package);

    public List<TableData> convertToTable(String pkgString);

}
