package com.factset.protobuf.stach.extensions;

public class Configurations {
    private static boolean SUPPRESS_SCIENTIFIC_NOTATION_FOR_DOUBLES = true;

    /**
     * The purpose of this function is to get the flag that suppresses double's default scientific notation of a value
     * @return Returns a boolean value, default is true
     */
    public static boolean getSuppressScientificNotationForDoubles(){
        return SUPPRESS_SCIENTIFIC_NOTATION_FOR_DOUBLES;
    }

    /**
     * The purpose of this function is to suppress double's default scientific notation of a value
     * @param value: Boolean value
     */
    public static void setSuppressScientificNotationForDoubles(boolean value){
        SUPPRESS_SCIENTIFIC_NOTATION_FOR_DOUBLES = value;
    }
}
