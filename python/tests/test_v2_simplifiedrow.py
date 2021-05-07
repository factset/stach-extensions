import json
import os
import unittest

from src.fds.protobuf.stach.extensions.StachExtensionFactory import StachExtensionFactory
from src.fds.protobuf.stach.extensions.StachVersion import StachVersion

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))


class V2StachTests(unittest.TestCase):
    def test_stachv2(self):
        headerColumns = ["total0", "group1", "group2", "Port.+Weight", "Bench.+Weight", "Difference"]
        firstRow = ['Total', '', '', 100.0, '', 100.0]
        file = os.path.join(ROOT_DIR, "resources", "V2SimplifiedRowStachData.json")
        with open(file) as f:
            data = json.load(f)
            stachBuilder = StachExtensionFactory.get_row_organized_builder(StachVersion.V2)
            stachExtension = stachBuilder.set_package(data).build()
            tables = stachExtension.convert_to_dataframe()

            self.assertEqual(headerColumns, tables[0].columns.values.tolist())
            self.assertEqual(firstRow, tables[0].iloc[0].values.tolist())


if __name__ == '__main__':
    unittest.main()
