import json
import os
import unittest

from fds.protobuf.stach.extensions.StachExtensionFactory import StachExtensionFactory
from fds.protobuf.stach.extensions.StachVersion import StachVersion
from fds.protobuf.stach.extensions.v2.ColumnOrganizedStachUtility import ColumnOrganizedStachUtilities

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))


class V2StachUtilsTests(unittest.TestCase):
    def test_stach_utils_v2(self):
        column_values = ["Port.+Weight","Bench.+Weight","Bench.+Weight","Bench.+Weight"]
        file = os.path.join(ROOT_DIR, "resources", "V2StachWithCompressedColumn.json")
        with open(file) as f:
            data = json.load(f)
            decompressed_data = ColumnOrganizedStachUtilities.decompress_column(data["data"]["columns"]["col1"])

            self.assertEqual(decompressed_data,column_values)
