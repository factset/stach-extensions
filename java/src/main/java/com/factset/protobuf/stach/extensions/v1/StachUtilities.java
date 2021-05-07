package com.factset.protobuf.stach.extensions.v1;

import com.factset.protobuf.stach.NullValues;
import com.factset.protobuf.stach.table.DataTypeProto;
import com.factset.protobuf.stach.table.SeriesDataProto;
import com.factset.protobuf.stach.table.SeriesDefinitionProto;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import java.util.List;

public class StachUtilities {

    /**
     * Returns the index of the element with given id in the list of SeriesDefinition objects
     *
     * @param seriesDefinitionList : List of SeriesDefinition objects.
     * @param id   : The id of SeriesDefinition object.
     * @return return the index of the object with the given id.
     */
    public static int getIndexOf(List<SeriesDefinitionProto.SeriesDefinition> seriesDefinitionList, String id) {
        int pos = 0;
        for (SeriesDefinitionProto.SeriesDefinition seriesDefinition : seriesDefinitionList) {
            if (id.equalsIgnoreCase(seriesDefinition.getId()))
                return pos;
            pos++;
        }
        return -1;
    }

    /**
     * The purpose of this function is to return the value from the provided SeriesData object.
     *
     * @param seriesData : The data of a series. A series is how a column of data is represented.
     * @param dataType   : The type of data in a series.
     * @param index      : index can be referred as a key which is used to access the value inside list.
     * @param nullFormat : nullFormat is used to render a null value with a special string, like -- or @NA.
     * @return Returns data object from the SeriesData.
     */
    public static Object getValueHelper(SeriesDataProto.SeriesData seriesData, DataTypeProto.DataType dataType, int index,
                                        String nullFormat) {
        if (dataType == DataTypeProto.DataType.STRING) {
            String value = seriesData.getStringArray().getValues(index);
            return NullValues.STRING.equals(value) ? nullFormat : value;
        } else if (dataType == DataTypeProto.DataType.DOUBLE) {
            double value = seriesData.getDoubleArray().getValues(index);
            return Double.isNaN(value) ? nullFormat : value;
        } else if (dataType == DataTypeProto.DataType.BOOL) {
            return seriesData.getBoolArray().getValues(index);
        } else if (dataType == DataTypeProto.DataType.DURATION) {
            Duration value = seriesData.getDurationArray().getValues(index);
            return NullValues.DURATION.equals(value) ? nullFormat : value;
        } else if (dataType == DataTypeProto.DataType.FLOAT) {
            float value = seriesData.getFloatArray().getValues(index);
            return Float.isNaN(value) ? nullFormat : value;
        } else if (dataType == DataTypeProto.DataType.INT32) {
            int value = seriesData.getInt32Array().getValues(index);
            return NullValues.INT32 == value ? nullFormat : value;
        } else if (dataType == DataTypeProto.DataType.INT64) {
            long value = seriesData.getInt64Array().getValues(index);
            return NullValues.INT64 == value ? nullFormat : value;
        } else if (dataType == DataTypeProto.DataType.TIMESTAMP) {
            Timestamp value = seriesData.getTimestampArray().getValues(index);
            return NullValues.TIMESTAMP.equals(value) ? nullFormat : value;
        } else {
            throw new NotImplementedException(dataType + " is not implemented");
        }
    }
}
