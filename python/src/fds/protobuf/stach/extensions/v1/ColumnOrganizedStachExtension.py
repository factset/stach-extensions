import pandas as pd
from fds.protobuf.stach.Package_pb2 import Package

from fds.protobuf.stach.extensions.IStachExtension import IStachExtension
from fds.protobuf.stach.extensions.v1.StachUtilities import StachUtilities


class ColumnOrganizedStachExtension(IStachExtension):
    """The purpose of this class is to provide the helper methods for converting Stach to Tabular format"""

    def __init__(self, package: Package):
        self.package = package

    def convert_to_dataframe(self):
        tables = list()
        for primary_table_id in self.package.primary_table_ids:
            tables.append(ColumnOrganizedStachExtension.__generate_table(self.package, primary_table_id))
        return tables

    def get_metadata(self):
        metadata = list()
        for primary_table_id in self.package.primary_table_ids:
            metadata.append(self.__generate_metadata(self.package, primary_table_id))
        return metadata

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
            row_count = len(primary_table.data.rows)

            headers = list(list())
            # Constructs the column headers by considering dimension columns and header rows
            for series_definition_column in header_table.definition.columns:
                header_row = list()
                for primary_table_definition in primary_table.definition.columns:
                    if (primary_table_definition.is_dimension == True):
                        header_row.append(primary_table_definition.description or primary_table_definition.name)
                        continue
                    headerColumnId = primary_table_definition.header_id
                    indexOfHeaderColumnId = [index for index in range(len(header_table.data.rows)) if
                                             header_table.data.rows[index].id == headerColumnId][0]
                    val = str(StachUtilities.get_value_helper(header_table.data.columns[series_definition_column.id],
                                                              series_definition_column.type, indexOfHeaderColumnId,
                                                              series_definition_column.format.null_format))
                    header_row.append(val)
                headers.append(header_row)

            data = list(list())
            # Constructs the column data
            for i in range(0, row_count, 1):
                data_row = list()
                for series_definition_column in primary_table.definition.columns:
                    data_row.append(str(
                        StachUtilities.get_value_helper(primary_table.data.columns[series_definition_column.id],
                                                        series_definition_column.type, i,
                                                        series_definition_column.format.null_format)))
                data.append(data_row)

            if len(header_table.definition.columns) > 1:
                data_frame = pd.DataFrame(data=data)
                data_frame.columns = pd.MultiIndex.from_arrays(headers)
            else:
                data_frame = pd.DataFrame(data=data, columns=headers[0])

            return data_frame

        else:
            ValueError("Response data passed should be of package type.")

    @staticmethod
    def __generate_metadata(package_response, primary_table_id):
        """
        The purpose of this function is to generate the metadata for the provided stach data through the package.

        :param package_response: Stach Data which is represented as a Package object.
        :param primary_table_id: Refers to the id for a particular table inside a package.
        :return: Returns the generated metadata from the package provided.
        """
        primary_table = package_response.tables[primary_table_id]
        metadata = {}

        for location in primary_table.data.metadata.locations.table:
            name = primary_table.data.metadata.items[location].name

            stringval = primary_table.data.metadata.items[location].string_value
            values = stringval.split("|")
            
            metadata[name] = values
        return metadata
