package com.factset.protobuf.stach.extensions;

public class Utilities {

    /**
     * The purpose of this function is to check if the string is empty or is null.
     *
     * @param str : The input string object.
     * @return Returns true if the string object is null or empty string.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
