import base64
import os
import unittest

import pyarrow as pa

from fds.protobuf.stach.extensions.v3.StachV3ArrowHelper import (
    _build_column_headers_mapping,
    _get_multi_level_reference,
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


class TestMultiLevelReferenceFallback(unittest.TestCase):
    """The multi-level reference must be found even when the Table protobuf does not provide it."""

    def setUp(self):
        self.mlh = pa.table({"header_table_primary_key": ["1"], "header_table_0": ["Q1"]})

    def test_reference_found_in_raw_table_metadata_when_protobuf_is_missing(self):
        raw_table = b"\x0a\x05junk" + b"header_table_primary_key" + b"\x12\x03end"
        metadata = {b"fds:stach:table": base64.b64encode(raw_table)}
        self.assertEqual(_get_multi_level_reference(metadata, None, self.mlh), "header_table_primary_key")

    def test_reference_taken_from_protobuf_when_available(self):
        class SplitResult:
            multi_level_headers_table_reference = "header_table_0"

        class Table:
            split_result = SplitResult()

        self.assertEqual(_get_multi_level_reference({}, Table(), self.mlh), "header_table_0")

    def test_no_reference_returns_empty_string(self):
        self.assertEqual(_get_multi_level_reference({}, None, self.mlh), "")


def _varint(n):
    out = bytearray()
    while True:
        byte = n & 0x7F
        n >>= 7
        if n:
            out.append(byte | 0x80)
        else:
            out.append(byte)
            return bytes(out)

def _field(number, payload):
    """Length-delimited protobuf field."""
    return _varint((number << 3) | 2) + _varint(len(payload)) + payload

class TestViewHeadersWithoutProtobuf(unittest.TestCase):
    """View headers must be readable from the raw Views message when the generated class is missing."""

    def _views_bytes(self, headers):
        entries = b"".join(
            _field(3, _field(1, key.encode()) + _field(2, value.encode())) for key, value in headers.items()
        )
        table = _field(2, b"0") + entries          # Table: columns (field 2) and headers map (field 3)
        view = _field(1, b"Default") + _field(3, table)
        return _field(1, view)                     # Views: repeated View views = 1

    def test_reads_headers_map_from_first_view(self):
        from fds.protobuf.stach.extensions.v3.StachV3ArrowHelper import _parse_view_headers_from_bytes
        data = self._views_bytes({"0": "Security Name", "1": "Port.\nWeight"})
        self.assertEqual(_parse_view_headers_from_bytes(data), {"0": "Security Name", "1": "Port.\nWeight"})

    def test_empty_views_message_returns_empty_dict(self):
        from fds.protobuf.stach.extensions.v3.StachV3ArrowHelper import _parse_view_headers_from_bytes
        self.assertEqual(_parse_view_headers_from_bytes(b""), {})


if __name__ == "__main__":
    unittest.main()
