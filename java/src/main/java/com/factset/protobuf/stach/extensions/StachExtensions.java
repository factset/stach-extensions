package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.models.TableData;

import java.util.List;

public interface StachExtensions<T> {

    List<TableData> convertToTable();

}
