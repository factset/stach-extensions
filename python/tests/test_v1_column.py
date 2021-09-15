import json
import os
import unittest

from fds.protobuf.stach.extensions.StachExtensionFactory import StachExtensionFactory
from fds.protobuf.stach.extensions.StachVersion import StachVersion

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))


class V1StachTests(unittest.TestCase):
    def test_stachv1(self):
        headerColumns = ["total0", "group1", "group2", "Port.+Weight", "Bench.+Weight", "Difference"]
        firstRow = ['Total', '', '', '100.0', '--', '100.0']
        file = os.path.join(ROOT_DIR, "resources", "V1ColumnStachData.json")
        with open(file) as f:
            data = json.load(f)
            stachBuilder = StachExtensionFactory.get_column_organized_builder(StachVersion.V1)
            stachExtension = stachBuilder.set_package(data).build()
            tables = stachExtension.convert_to_dataframe()

            self.assertEqual(headerColumns, tables[0].columns.values.tolist())
            self.assertEqual(firstRow, tables[0].iloc[0].values.tolist())

    def test_stachv1_metadata(self):
        file = os.path.join(ROOT_DIR, "resources", "V1ColumnStachData.json")
        with open(file) as f:
            data = json.load(f)
            stachBuilder = StachExtensionFactory.get_column_organized_builder(StachVersion.V1)
            stachExtension = stachBuilder.set_package(data).build()
            metadata = stachExtension.get_metadata()

            self.assertEqual(18, len(metadata[0]))
            self.assertEqual("Single", metadata[0]["Report Frequency"][0])
            self.assertEqual("Industry - Beginning of Period", metadata[0]["Grouping Frequency"][1])

if __name__ == '__main__':
    unittest.main()
