package com.factset.protobuf.stach.extensions.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.protobuf.Value;

public class TableData {
    private List<Row> Rows = new ArrayList<Row>();
    private Map<String, String> metadata = new HashMap<String, String>();
    private Map<String, List<Value>> rawMetadata = new HashMap<String, List<Value>>();
    private Map<String, List<String>> metadataArray = new HashMap<String, List<String>>();


    public List<Row> getRows() {
        return Rows;
    }

    public void setRows(List<Row> rows) {
        this.Rows = rows;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Map<String, List<String>> getMetadataArray() {
        return metadataArray;
    }

    public Map<String, List<Value>> getRawMetadata() { return rawMetadata; }

    public void setMetadata(Map<String, String> map) {
        this.metadata = map;
    }

}
