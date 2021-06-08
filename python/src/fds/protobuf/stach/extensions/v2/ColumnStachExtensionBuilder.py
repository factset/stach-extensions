import json

import google.protobuf.json_format
from fds.protobuf.stach.v2.Package_pb2 import Package

from fds.protobuf.stach.extensions.v2.ColumnOrganizedStachExtension import ColumnOrganizedStachExtension


class ColumnStachExtensionBuilder:

    def set_package(self, pkg):
        """
        Sets the package to the stach extension builder. The input package can be instance of Package, or
        string or object representation of the Package.

        :param pkg: Package object in string or object form.
        :return: builder instance
        """
        if (type(pkg) is Package):
            self.package = pkg
        elif (type(pkg) is str):
            package = Package()
            google.protobuf.json_format.Parse(pkg, package, ignore_unknown_fields=True, descriptor_pool=None)
            self.package = package
        else:
            pkg = json.dumps(pkg)
            package = Package()
            google.protobuf.json_format.Parse(pkg, package, ignore_unknown_fields=True, descriptor_pool=None)
            self.package = package

        return self

    def get_package(self):
        """
        Returns the Package object set for the builder

        :return: Package instance
        """
        if (not hasattr(self, "package")):
            return None
        return self.package

    def build(self):
        """
        Builds and returns the instance of ColumnOrganizedStachExtension class

        """
        return ColumnOrganizedStachExtension(self.package)
