import json

import google.protobuf.json_format
from fds.protobuf.stach.Package_pb2 import Package

from src.fds.protobuf.stach.extensions.v1.ColumnOrganizedStachExtension import ColumnOrganizedStachExtension


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
        return self;

    def build(self):
        """
        Builds and returns the instance of ColumnOrganizedStachExtension class

        """
        return ColumnOrganizedStachExtension(self.package)
