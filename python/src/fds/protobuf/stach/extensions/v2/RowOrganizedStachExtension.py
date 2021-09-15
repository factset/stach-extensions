import numpy as np
import pandas as pd
from fds.protobuf.stach.v2.RowOrganized_pb2 import RowOrganizedPackage

from fds.protobuf.stach.extensions.IStachExtension import IStachExtension
from fds.protobuf.stach.extensions.v2.StachUtilities import StachUtilities


class RowOrganizedStachExtension(IStachExtension):

    def __init__(self, package: RowOrganizedPackage):
        self.package = package

    def get_package(self):
        return self.package

    def convert_to_dataframe(self):
        tables = list()
        for tableId in self.package.tables:
            tables.append(RowOrganizedStachExtension.__generate_table(self.package.tables[tableId]))
        return tables

    def get_metadata(self):
        metadata = list()
        for tableId in self.package.tables:
            metadata.append(RowOrganizedStachExtension.__generate_metadata(self.package.tables[tableId]))
        return metadata

    @staticmethod
    def __generate_table(table):
        """
        The purpose of this function is to generate Tables for the provided stach data through the package.

        :param package_response: Stach Data which is represented as a Package object.
        :param primary_table_id: Refers to the id for a particular table inside a package.
        :return: Returns the generated Table from the package provided.
        """
        if isinstance(table, RowOrganizedPackage.Table):
            rowIndex = 0
            headers = list(list())
            data = list(list())

            rowType = table.data.rows[0].row_type
            if (RowOrganizedPackage.Row.RowType.Name(rowType) != "Header"):
                header = list()
                for column_definition in table.definition.columns:
                    header.append(column_definition.description or column_definition.name)
                headers.append(header)

            # The row_spread_tuple_list contains list of tuples and each tuple has the information about
            # the items that has to be spread using the rowspan value.
            # Each tuple should contain 4 values as mentioned below
            #   postion - position at which the item needs to be added
            #   rowspan - the number of rows across which the value has to be spread
            #   colspan - the number of columns across which the value has to be spread
            #   val     - value
            row_spread_tuple_list = list(tuple())

            for iRowIndex in range(rowIndex, len(table.data.rows), 1):
                currentRow = table.data.rows[iRowIndex]
                data_row = list()

                if (RowOrganizedPackage.Row.RowType.Name(currentRow.row_type) == "Header"):
                    header = list()
                    index = 0       # The index of values in the the header row cells list.
                    position = 0    # The actual column position (by considering the rowspan, colspan spreading).

                    # Checking and adding values at the start of row based on rowspan spread details from previously processed rows.
                    # Once processed, we get the values to be added and the updated position.
                    rowSpannedHeader, position = StachUtilities.check_add_rowspan_items(position, iRowIndex, row_spread_tuple_list)

                    if rowSpannedHeader is not None:
                        header.extend(rowSpannedHeader)
                    for val in currentRow.cells:

                        headerCellDetail = list(currentRow.header_cell_details._values.values())[index]
                        colspan = headerCellDetail.colspan
                        colspan = 1 if colspan <= 1 else colspan
                        rowspan = headerCellDetail.rowspan
                        rowspan = 1 if rowspan <= 1 else rowspan

                        # if rowspan > 1, ie., the value has to be spanned across multiple rows.
                        # Storing the info about the position at which the item has to be added, how many rows
                        # it has to be spanned, number of columns it has to be spanned and the actual value that needs
                        # to be spanned.
                        if (rowspan > 1):
                            row_spread_tuple_list.append((position, rowspan, colspan, val))

                        for i in range(0, colspan):
                            header.append(val)
                            position = position + 1     # incrementing column position after adding item to header row

                            # After incrementing column position, checking if any value has to be added at the new
                            # column position based on the row spread list.
                            rowSpannedHeader, position = StachUtilities.check_add_rowspan_items(position, iRowIndex, row_spread_tuple_list)
                            if rowSpannedHeader is not None:
                                header.extend(rowSpannedHeader)

                        index = index + 1

                    headers.append(header)
                    continue
                else:
                    for column_definition in table.definition.columns:
                        val = table.data.rows[iRowIndex].values[column_definition.id]
                        val = StachUtilities.get_value(val)
                        data_row.append(val if val is not None else column_definition.format.null_format)
                data.append(data_row)

            if len(headers) > 1:
                data_frame = pd.DataFrame(data=data)
                data_frame.columns = pd.MultiIndex.from_arrays(headers)
            else:
                data_frame = pd.DataFrame(data=data, columns=headers[0])

            data_frame = data_frame.replace({np.nan: None})

            return data_frame

    @staticmethod
    def __generate_metadata(table):
        """
        The purpose of this function is to generate the metadata for the provided stach data through the package.

        :param table: A particular table inside a package.
        :return: Returns the generated metadata from the package provided.
        """
        metadata = {}

        for metadataItem in table.data.table_metadata:
            values = list()
            for value in table.data.table_metadata[metadataItem].value.list_value.values:
                values.append(value)

            metadata[metadataItem] = values

        return metadata