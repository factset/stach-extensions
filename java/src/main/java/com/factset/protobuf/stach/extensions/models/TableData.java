package com.factset.protobuf.stach.extensions.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableData {
    private List<Row> Rows = new ArrayList<Row>();
    private Map<String, String> metadata = new HashMap<String, String>();
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

    public void setMetadata(Map<String, String> map) {
        this.metadata = map;
    }

    public void setMetadataArray(Map<String, List<String>> map) {
        this.metadataArray = map;
    }
}
