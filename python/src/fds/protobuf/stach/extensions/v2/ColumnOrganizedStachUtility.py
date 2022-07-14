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
    def decompress_page(result_pages, table_id):
        """
        This method parses data pages, decompressing columns
        :param result_pages: the data page to be decompressed
        :return result_page: the decompressed data page
        """
        for page in result_pages:
            print(page)
            for column in result_pages["tables"][table_id]["definition"]["columns"]:
                column_data = result_pages[page]["tables"]["table"]["data"]["columns"][column["id"]]
                column_data["values"] = ColumnOrganizedStachUtilities.decompress_column(column_data)
        return result_pages


