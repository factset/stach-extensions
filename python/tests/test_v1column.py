import json
import os
import unittest

from fds.protobuf.stach.Package_pb2 import Package
from google.protobuf import json_format

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))

from src.fds.protobuf.stach.extensions.v1.ColumnOrganizedStachExtension import ColumnOrganizedStachExtension

class V1StachTests(unittest.TestCase):
    def test_stachv1(self):
        headerColumns = ["total0", "group1", "group2", "Port.+Weight", "Bench.+Weight", "Difference"]
        firstRow = ['Total', '', '', '100.0', '--', '100.0']
        file = os.path.join(ROOT_DIR, "resources", "V1Column.json")
        with open(file) as f:
            data = json.load(f)
            stachExtensions = ColumnOrganizedStachExtension()

            result = json_format.Parse(json.dumps(data), Package())
            tables = stachExtensions.convert_to_table_format(result)  # To convert result to 2D tables.

            self.assertEqual(headerColumns, tables[0].columns.values.tolist())
            self.assertEqual(firstRow, tables[0].iloc[0].values.tolist())


if __name__ == '__main__':
    unittest.main()
