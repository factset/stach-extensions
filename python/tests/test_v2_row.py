import json
import os
import unittest

from fds.protobuf.stach.extensions.StachVersion import StachVersion
from fds.protobuf.stach.extensions.StachExtensionFactory import StachExtensionFactory

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))


class V2StachTests(unittest.TestCase):
    def test_stachv2(self):
        headerColumns = ["total0", "group1", "group2", "Difference"]
        firstRow = ['Total', '', '', '--']
        file = os.path.join(ROOT_DIR, "resources", "V2RowStachData.json")
        with open(file) as f:
            data = json.load(f)
            stachBuilder = StachExtensionFactory.get_row_organized_builder(StachVersion.V2)
            stachExtension = stachBuilder.set_package(data).build()
            tables = stachExtension.convert_to_dataframe()

            self.assertEqual(headerColumns, tables[0].columns.values.tolist())
            self.assertEqual(firstRow, tables[0].iloc[0].values.tolist())

    def test_stachv2_add_table(self):
        headerColumns = ["total0", "group1", "group2", "Difference"]
        firstRow = ['Total', '', '', '--']
        file = os.path.join(ROOT_DIR, "resources", "V2RowStachData.json")
        with open(file) as f:
            data = json.load(f)
            table = data['tables']['3805a4c8-5493-4e63-9d71-c553d20f266b']
            stachBuilder = StachExtensionFactory.get_row_organized_builder(StachVersion.V2)
            stachExtension = stachBuilder.add_table("id1", table).build()
            tables = stachExtension.convert_to_dataframe()

            self.assertEqual(headerColumns, tables[0].columns.values.tolist())
            self.assertEqual(firstRow, tables[0].iloc[0].values.tolist())


if __name__ == '__main__':
    unittest.main()
