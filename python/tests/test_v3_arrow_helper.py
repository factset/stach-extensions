import os
import unittest

from fds.protobuf.stach.extensions.v3.StachV3ArrowHelper import (
    _build_column_headers_mapping,
    _populate_dataframe,
    get_arrow_stream,
)

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
ARROW_FILE = os.path.join(ROOT_DIR, "resources", "V3ArrowFileStachResponse.arrow")


class TestDuplicateHeaders(unittest.TestCase):
    """Two Arrow fields resolving to the same display header must not break DataFrame creation."""

    @classmethod
    def setUpClass(cls):
        with open(ARROW_FILE, "rb") as f:
            cls.arrow_bytes = f.read()
        cls.schema = get_arrow_stream(cls.arrow_bytes).schema
        cls.field_names = cls.schema.names

    def test_colliding_view_headers_are_made_unique(self):
        first, second = self.field_names[0], self.field_names[1]
        mapping = {}
        _build_column_headers_mapping(self.schema, {first: "Same", second: "Same"}, {}, mapping)

        headers = list(mapping.values())
        self.assertEqual(len(headers), len(set(headers)), f"headers are not unique: {headers}")
        self.assertEqual(mapping[first], "Same")
        self.assertEqual(mapping[second], "Same.1")

    def test_populate_dataframe_with_colliding_headers_keeps_all_rows(self):
        first, second = self.field_names[0], self.field_names[1]
        mapping = {}
        _build_column_headers_mapping(self.schema, {first: "Same", second: "Same"}, {}, mapping)

        reader = get_arrow_stream(self.arrow_bytes)
        data_table = _populate_dataframe(reader, mapping)

        counter = get_arrow_stream(self.arrow_bytes)
        expected_rows = sum(counter.get_batch(i).num_rows for i in range(counter.num_record_batches))
        self.assertEqual(len(data_table), expected_rows)
        self.assertEqual(list(data_table.columns), list(mapping.values()))


if __name__ == "__main__":
    unittest.main()
