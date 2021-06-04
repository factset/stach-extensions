package com.factset.protobuf.stach.extensions.v2;

import com.factset.protobuf.stach.v2.table.RowDefinitionProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;

import java.util.List;

public class StachUtilities {

    /**
     * Returns the respective data type object from the Value object
     *
     * @param value : protobuf Value object input
     * @return returns the respective data type object from the input
     * @throws InvalidProtocolBufferException 
     */
    public static Object valueToObject(Value value) throws InvalidProtocolBufferException {
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
                //return value.getStructValue().toString();
            	return JsonFormat.printer().omittingInsignificantWhitespace().print(value.getStructValue());
            case LIST_VALUE:
                return value.getListValue().getValuesList().toString();
            default:
                throw new IllegalArgumentException(String.format("Unsupported protobuf value %s", value));
        }
    }

    /**
     * Returns the index of the element with given id in the list of RowDefinition objects
     *
     * @param rowDefinitionsList : List of RowDefinition objects.
     * @param id   : The id of the RowDefinition object.
     * @return return the index of the object with the given id.
     */
    public static int getIndexOf(List<RowDefinitionProto.RowDefinition> rowDefinitionsList, String id) {
        int pos = 0;
        for (RowDefinitionProto.RowDefinition rowDefinition : rowDefinitionsList) {
            if (id.equalsIgnoreCase(rowDefinition.getId()))
                return pos;
            pos++;
        }
        return -1;
    }
}
