package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.table.SeriesDefinitionProto;
import com.factset.protobuf.stach.v2.table.RowDefinitionProto;
import com.google.protobuf.Value;

import java.util.List;

public class StachUtilities {

    /**
     * Returns the respective data type object from the Value object
     * @param value  : protobuf Value object input
     * @return returns the respective data type object from the input
     */
    public Object valueToObject(Value value) {
        switch (value.getKindCase()) {
            case NULL_VALUE:
                return null;
            case NUMBER_VALUE:
                return "" + value.getNumberValue();
            case STRING_VALUE:
                return value.getStringValue();
            case BOOL_VALUE:
                return Boolean.toString(value.getBoolValue());
            case STRUCT_VALUE:
                return value.getStructValue().toString();
            case LIST_VALUE:
                return value.getListValue().getValuesList().toString();
            default:
                throw new IllegalArgumentException(String.format("Unsupported protobuf value %s", value));
        }
    }

    /**
     * Returns the index of the element with given id in the list of RowDefinition or SeriesDefinition objects
     * @param list : List of RowDefinition or SeriesDefinition objects.
     * @param id   : The id of the RowDefinition or the SeriesDefinition object.
     * @return return the index of the object with the given id.
     */
    public static int getIndexOf(List list, String id) {
        int pos = 0;
        if(list.get(0) instanceof RowDefinitionProto.RowDefinition ){
            List<RowDefinitionProto.RowDefinition> convertedList = (List<RowDefinitionProto.RowDefinition>)list;
            for (RowDefinitionProto.RowDefinition myObj : convertedList) {
                if (id.equalsIgnoreCase(myObj.getId()))
                    return pos;
                pos++;
            }
        }
        if(list.get(0) instanceof SeriesDefinitionProto.SeriesDefinition ){
            List<SeriesDefinitionProto.SeriesDefinition> convertedList = (List<SeriesDefinitionProto.SeriesDefinition>)list;
            for (SeriesDefinitionProto.SeriesDefinition myObj : convertedList) {
                if (id.equalsIgnoreCase(myObj.getId()))
                    return pos;
                pos++;
            }
        }
        return -1;
    }

    /**
     * The purpose of this function is to check if the string is empty or is null.
     * @param str : The input string object.
     * @return Returns true if the string object is null or empty string.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
