package com.factset.protobuf.stach.extensions.v3;

import java.util.List;
import java.util.Map;

public class TableSchema {
    public String Name;
    public String Version;
    public List<Map<String, Object>> Columns;
    public List<Map<String, String>> Rows;
    public String ViewName;
    public Map<String, String> ViewHeaders;
    public List<String> ViewColumns;
    public Map<String, Object> GroupResult;
    public Map<String, Object> SplitResult;
    public Map<String, Object> PrimaryKeys;
}
