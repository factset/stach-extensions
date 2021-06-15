from google.protobuf import json_format
from google.protobuf.struct_pb2 import Struct

class StachUtilities:
    @staticmethod
    def check_add_rowspan_items(position, rowIndex, row_spread_tuple_list):
        """
         This method checks if any value has to be added at the given column position and rowindex by looking up in
         the row_spread_tuple_list and returns the list of values that needs to be added and the updated column position

        :param position: The column position at which the value has to be spread
        :param rowIndex: The index of the row
        :param row_spread_tuple_list: list that contains info about the values that needs to be spread at the given column position and row
        :return: returns the list of values that needs to be added using the rowspan and the updated position
        """
        rowSpannedHeader = list()

        if len(row_spread_tuple_list) == 0:
            return (None, position)

        # Finding the row spread tuple from the list which is matching the column position and row index
        result = [t for t in row_spread_tuple_list if (t[0] == position and rowIndex < t[1])]
        if len(result) == 0:
            return (None, position)

        result = result[0]

        if (result is not None):
            for i in range(0, result[2]):

                # Appending the value to be spread and updating the column position by 1
                rowSpannedHeader.append(result[3])
                position = position + 1

                # Checking and adding if value has to be spread at the updated column position recursively.
                rowSpannedHeaderNextLevel, position = StachUtilities.check_add_rowspan_items(position, rowIndex, row_spread_tuple_list)
                if rowSpannedHeaderNextLevel is not None:
                    rowSpannedHeader.extend(rowSpannedHeaderNextLevel)

        return (rowSpannedHeader, position)


    @staticmethod
    def get_value(val):
        if(type(val) is Struct):
            return str(json_format.MessageToDict(val))
        else:
            return val

