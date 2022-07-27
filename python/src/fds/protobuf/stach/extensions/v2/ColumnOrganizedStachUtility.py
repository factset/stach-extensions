from google.protobuf import json_format
from google.protobuf.struct_pb2 import Struct

class ColumnOrganizedStachUtilities:
    @staticmethod
    def decompress_column(column):
        """
        This method is used to decompress a stach response column
        :param column: The column to be decompressed
        :return decompressed_column: returns the decompressed column
        """

        if "ranges" in column:
            decompressed_column = column["values"].copy()
            for index, length in column["ranges"].items():
                index = int(index)
                decompressed_column[index:index] = [decompressed_column[index]] * (length - 1)
            return decompressed_column
        else:
            return column["values"]

    @staticmethod
    def decompress_all_columns(pkg, table_id):
        """
        This method parses results, decompressing all columns in a table
        :param pkg: the package object to be decompressed
        :param table_id: the id of the table whose columns are to be decompressed
        :return pkg: the decompressed package object
        """
        for column in pkg["tables"][table_id]["definition"]["columns"]:
            column_data = pkg["tables"][table_id]["data"]["columns"][column["id"]]
            column_data["values"] = ColumnOrganizedStachUtilities.decompress_column(column_data)
        return pkg


