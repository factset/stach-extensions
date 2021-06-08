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

            for i in range(rowIndex, len(table.data.rows), 1):
                currentRow = table.data.rows[i]
                data_row = list()
                if (RowOrganizedPackage.Row.RowType.Name(currentRow.row_type) == "Header"):
                    header = list()
                    for val in currentRow.cells:
                        header.append(val)
                    headers.append(header)
                    continue
                else:
                    for column_definition in table.definition.columns:
                        val = table.data.rows[i].values[column_definition.id]
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
