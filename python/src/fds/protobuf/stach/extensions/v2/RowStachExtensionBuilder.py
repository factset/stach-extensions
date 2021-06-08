import json

import google.protobuf.json_format
from fds.protobuf.stach.v2.RowOrganized_pb2 import RowOrganizedPackage

from fds.protobuf.stach.extensions.v2.RowOrganizedStachExtension import RowOrganizedStachExtension


class RowStachExtensionBuilder:

    def set_package(self, pkg):
        """
        Sets the package to the stach extension builder. The input package can be instance of Package, or
        string or object representation of the Package.

        :param pkg: Package object in string or object form.
        :return: builder instance
        """
        if (type(pkg) is RowOrganizedPackage):
            self.package = pkg
        elif (type(pkg) is str):
            package = RowOrganizedPackage()
            google.protobuf.json_format.Parse(pkg, package, ignore_unknown_fields=True, descriptor_pool=None)
            self.package = package
        else:
            pkg = json.dumps(pkg)
            package = RowOrganizedPackage()
            google.protobuf.json_format.Parse(pkg, package, ignore_unknown_fields=True, descriptor_pool=None)
            self.package = package

        return self

    def add_table(self, id, table):
        """
        Adds the table to the builder for conversion.

        :param id: id of the table.
        :param table: the inuput table object to be converted.
        :return: builder instance
        """
        if (type(table) is RowOrganizedPackage.Table):
            # we are checking if self.package is available, so that we can add the given table to the existing package.
            # if self.package is not available, we do create a package and add table to it.
            if (not hasattr(self, "package")):
                self.package = RowOrganizedPackage()
            empty_table = self.package.tables.get_or_create(id)
            empty_table.CopyFrom(table)
            return self
        elif (type(table) is str):
            pass
        else:
            table = json.dumps(table)

        tableInstance = RowOrganizedPackage.Table()
        google.protobuf.json_format.Parse(table, tableInstance, ignore_unknown_fields=True, descriptor_pool=None)

        # we are checking if self.package is available, so that we can add the given table to the existing package.
        # if self.package is not available, we do create a package and add table to it.
        if (not hasattr(self, "package")):
            self.package = RowOrganizedPackage()
        empty_table = self.package.tables.get_or_create(id)
        empty_table.CopyFrom(tableInstance)

        return self

    def get_package(self):
        """
        Returns the RowOrganizedPackage object set for the builder

        :return: RowOrganizedPackage instance
        """
        if (not hasattr(self, "package")):
            return None
        return self.package

    def build(self):
        """
        Builds and returns the instance of ColumnOrganizedStachExtension class

        """
        return RowOrganizedStachExtension(self.package)
