package com.factset.protobuf.stach.extensions.v2;

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
    public static Object valueToObject(Value value) {
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
     * Returns the index of the element with given id in the list of RowDefinition objects
     * @param list : List of RowDefinition objects.
     * @param id   : The id of the RowDefinition object.
     * @return return the index of the object with the given id.
     */
    public static int getIndexOf(List<RowDefinitionProto.RowDefinition> list, String id) {
        int pos = 0;
        for (RowDefinitionProto.RowDefinition myObj : list) {
            if (id.equalsIgnoreCase(myObj.getId()))
                return pos;
            pos++;
        }
        return -1;
    }
}
