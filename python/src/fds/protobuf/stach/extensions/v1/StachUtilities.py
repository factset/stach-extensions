import math

from fds.protobuf.stach.NullValues import NullValues
from fds.protobuf.stach.table.DataType_pb2 import DataType


class StachUtilities:
    """ This class provides helper method for returning the data from the SeriesData object by handling the null values. """

    @staticmethod
    def get_value_helper(series_data, datatype, index, null_format):
        """
        The purpose of this function is to return the value from the provided SeriesData object.

        :param series_data: The data of a series. A series is how a column of data is represented.
        :param datatype: The type of data in a series.
        :param index: index can be referred as a key which is used to access the value inside list.
        :param null_format: null_format is used to render a null value with a special string, like -- or @NA.
        :return: Return data object from the SeriesData.
        """
        if DataType.Name(datatype) == "STRING":
            return StachUtilities.null_value_handler(datatype, series_data.string_array.values[index], null_format)
        elif DataType.Name(datatype) == "DOUBLE":
            return StachUtilities.null_value_handler(datatype, series_data.double_array.values[index], null_format)
        elif DataType.Name(datatype) == "FLOAT":
            return StachUtilities.null_value_handler(datatype, series_data.float_array.values[index], null_format)
        elif DataType.Name(datatype) == "INT32":
            return StachUtilities.null_value_handler(datatype, series_data.int32_array.values[index], null_format)
        elif DataType.Name(datatype) == "INT64":
            return StachUtilities.null_value_handler(datatype, series_data.int64_array.values[index], null_format)
        elif DataType.Name(datatype) == "BOOL":
            return StachUtilities.null_value_handler(datatype, series_data.bool_array.values[index], null_format)
        elif DataType.Name(datatype) == "DURATION":
            return StachUtilities.null_value_handler(datatype, series_data.duration_array.values[index], null_format)
        elif DataType.Name(datatype) == "TIMESTAMP":
            return StachUtilities.null_value_handler(datatype, series_data.timestamp_array.values[index], null_format)
        else:
            ValueError("The datatype is not implemented")

    @staticmethod
    def null_value_handler(datatype, value, null_format):
        """
        The purpose of this function is to handle the null values.

        :param datatype: The type of data in a series.
        :param value: Specifies a particular data of a series referenced by an index.
        :param null_format: null_format is used to render a null value with a special string, like -- or @NA.
        :return: Returns either null value with a special string, like -- or @NA or the value at the particular index.
        """
        if DataType.Name(datatype) == "STRING":
            if NullValues.STRING == value:
                return null_format
            return value
        elif DataType.Name(datatype) == "DOUBLE":
            if math.isnan(value):
                return null_format
            return value
        elif DataType.Name(datatype) == "FLOAT":
            if math.isnan(value):
                return null_format
            return value
        elif DataType.Name(datatype) == "INT32":
            if NullValues.INT32 == value:
                return null_format
            return value
        elif DataType.Name(datatype) == "INT64":
            if NullValues.INT64 == value:
                return null_format
            return value
        elif DataType.Name(datatype) == "DURATION":
            if NullValues.DURATION.equals(value):
                return null_format
            return value
        elif DataType.Name(datatype) == "TIMESTAMP":
            if NullValues.TIMESTAMP.equals(value):
                return null_format
            return value
        else:
            return value
