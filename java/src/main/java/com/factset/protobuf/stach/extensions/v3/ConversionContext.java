package com.factset.protobuf.stach.extensions.v3;

import com.factset.protobuf.stach.v3.TableProto.Table;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConversionContext {
    public final List<Map<String, String>> dataTable = new ArrayList<>();
    public final LinkedHashMap<String, String> columnHeadersMapping = new LinkedHashMap<>();
    public Map<String, String> viewHeaders = new LinkedHashMap<>();
    public Table stachTable;
    public final LinkedHashMap<String, String> concatColumnsList = new LinkedHashMap<>();
}