import json
import os
import unittest

from fds.protobuf.stach.extensions.StachV3ExtensionFactory import (
    ConvertArrowFileToTable,
    ConvertArrowStreamToTable,
    ConvertJsonToTable,
)

RESOURCES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "resources")
ARROW_STREAM_FILE = os.path.join(RESOURCES, "V3MultiLevelHeaderArrowStreamResponse.arrow")
JSON_FILE = os.path.join(RESOURCES, "V3MultiLevelHeaderJsonResponse.json")

EXPECTED_ROW_COUNT = 9521
EXPECTED_HEADERS = [
    "group0", "group1", "group2", "group3", "group4", "group5",
    "Security Name",
    "26-OCT-2026 | Port. Weight",
    "26-OCT-2026 | Bench Weight",
    "26-OCT-2026 | Variation in Ending Weight",
    "3-string",
    "27-OCT-2026 | Port. Weight",
    "27-OCT-2026 | Bench Weight",
    "27-OCT-2026 | Variation in Ending Weight",
    "6-string",
]


def normalize_header(header):
    """Collapse line breaks and repeated spaces, so 'Port.\\nWeight' compares equal to 'Port. Weight'."""
    return " ".join(str(header).split())


def normalized_headers(table):
    return [normalize_header(column) for column in table.columns]


class TestMultiLevelHeadersArrow(unittest.TestCase):

    def test_arrow_stream_headers_match_expected(self):
        with open(ARROW_STREAM_FILE, "rb") as f:
            table = ConvertArrowStreamToTable(f.read())["table"]
        self.assertEqual(normalized_headers(table), EXPECTED_HEADERS)

    def test_arrow_stream_row_count_matches_expected(self):
        with open(ARROW_STREAM_FILE, "rb") as f:
            table = ConvertArrowStreamToTable(f.read())["table"]
        self.assertEqual(len(table), EXPECTED_ROW_COUNT)

    def test_arrow_file_path_headers_match_expected(self):
        table = ConvertArrowFileToTable(ARROW_STREAM_FILE)["table"]
        self.assertEqual(normalized_headers(table), EXPECTED_HEADERS)


class TestMultiLevelHeadersJson(unittest.TestCase):

    def _read_json_text(self):
        with open(JSON_FILE, encoding="utf-8") as f:
            return f.read()

    def test_json_resource_is_complete_json(self):
        try:
            json.loads(self._read_json_text())
        except json.JSONDecodeError as e:
            self.fail(f"V3MultiLevelHeaderJsonResponse.json is incomplete or invalid JSON: {e}")

    def test_json_headers_match_expected(self):
        table = ConvertJsonToTable(self._read_json_text())["table"]
        self.assertEqual(normalized_headers(table), EXPECTED_HEADERS)

    def test_json_row_count_matches_expected(self):
        table = ConvertJsonToTable(self._read_json_text())["table"]
        self.assertEqual(len(table), EXPECTED_ROW_COUNT)


class TestMultiLevelHeadersArrowVsJson(unittest.TestCase):

    def test_arrow_and_json_headers_match(self):
        with open(ARROW_STREAM_FILE, "rb") as f:
            arrow_headers = normalized_headers(ConvertArrowStreamToTable(f.read())["table"])
        with open(JSON_FILE, encoding="utf-8") as f:
            json_headers = normalized_headers(ConvertJsonToTable(f.read())["table"])
        self.assertEqual(arrow_headers, json_headers)


if __name__ == "__main__":
    unittest.main()
