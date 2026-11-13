package com.factset.protobuf.stach.extensions.v3;

import java.util.Arrays;
import java.util.List;

public class StachV3Constants {

	// constants
    public final String columnSeparator = " | ";
    public final String groupPrefix = "group";
    public final String levelNameColumn = "level";
    public final String securityNameColumn = "security";
    public final List<String> ignoredColumns = Arrays.asList("primary_key", "row_path",
            "level",
            "aggregate_rows",
            //"Security Name",
            "pk", "rp", "ag", "lvl");
}
