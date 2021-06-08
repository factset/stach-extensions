import pandas as pd
from fds.protobuf.stach.v2.Package_pb2 import Package

from fds.protobuf.stach.extensions.IStachExtension import IStachExtension


class ColumnOrganizedStachExtension(IStachExtension):

    def __init__(self, package: Package):
        self.package = package

    def convert_to_dataframe(self):
        tables = list()
        for primary_table_id in self.package.primary_table_ids:
            tables.append(self.__generate_table(self.package, primary_table_id))
        return tables

    @staticmethod
    def __generate_table(package_response, primary_table_id):
        """
        The purpose of this function is to generate Tables for the provided stach data through the package.

        :param package_response: Stach Data which is represented as a Package object.
        :param primary_table_id: Refers to the id for a particular table inside a package.
        :return: Returns the generated Table from the package provided.
        """
        if isinstance(package_response, Package):
            primary_table = package_response.tables[primary_table_id]
            header_id = primary_table.definition.header_table_id
            header_table = package_response.tables[header_id]
            headers = list(list())

            # Construct header rows
            for header_table_definition in header_table.definition.columns:
                header_row = list()

                for primary_table_definition in primary_table.definition.columns:
                    if (primary_table_definition.is_dimension == True):
                        header_row.append(primary_table_definition.description or primary_table_definition.name)
                        continue

                    headerColumnId = primary_table_definition.header_id
                    indexOfHeaderColumnId = [index for index in range(len(header_table.data.rows)) if
                                             header_table.data.rows[index].id == headerColumnId][0]
                    val = header_table.data.columns[header_table_definition.id].values[indexOfHeaderColumnId]
                    header_row.append(val)

                headers.append(header_row)

            # Construct data rows
            data = list(list())
            rowCount = len(primary_table.data.rows)
            for i in range(0, rowCount, 1):
                data_row = list()
                for primary_table_definition in primary_table.definition.columns:
                    val = primary_table.data.columns[primary_table_definition.id].values[i]
                    data_row.append(val if val is not None else primary_table_definition.format.null_format)
                data.append(data_row)

            if len(header_table.definition.columns) > 1:
                data_frame = pd.DataFrame(data=data)
                data_frame.columns = pd.MultiIndex.from_arrays(headers)
            else:
                data_frame = pd.DataFrame(data=data, columns=headers[0])
            return data_frame
