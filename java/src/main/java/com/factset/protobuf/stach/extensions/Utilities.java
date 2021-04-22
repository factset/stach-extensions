package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.table.SeriesDefinitionProto;
import com.factset.protobuf.stach.v2.table.RowDefinitionProto;
import com.google.protobuf.Value;

import java.util.List;

public class Utilities {

    /**
     * The purpose of this function is to check if the string is empty or is null.
     * @param str : The input string object.
     * @return Returns true if the string object is null or empty string.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
