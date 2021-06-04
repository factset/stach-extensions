from fds.protobuf.stach.extensions.v1.ColumnStachExtensionBuilder import \
    ColumnStachExtensionBuilder as V1ColumnStachExtensionBuilder
from fds.protobuf.stach.extensions.v2.ColumnStachExtensionBuilder import \
    ColumnStachExtensionBuilder as V2ColumnStachExtensionBuilder
from fds.protobuf.stach.extensions.StachVersion import StachVersion
from fds.protobuf.stach.extensions.v2.RowStachExtensionBuilder import RowStachExtensionBuilder

class StachExtensionFactory:

    @staticmethod
    def get_column_organized_builder(version: StachVersion):
        """
        Returns the respective stach extension builder instance based on specified version.
        version can be any of 'v1' or 'v2'

        :param version: StachVersion enum
        :return: instance of ColumnStachExtensionBuilder class.
        """
        if (version == StachVersion.V1):
            return V1ColumnStachExtensionBuilder()
        if (version == StachVersion.V2):
            return V2ColumnStachExtensionBuilder()
        return None

    @staticmethod
    def get_row_organized_builder(version: StachVersion):
        """
        Returns the stach extension builder instance.

        :return: instance of RowStachExtensionBuilder class.
        """
        if (version == StachVersion.V1):
            raise AttributeError("v1 version is not supported for row organized stach input.")
        if (version == StachVersion.V2):
            return RowStachExtensionBuilder()
        return None
