"""End-user consumer for the Python SDK.

Runs OUTSIDE the source tree and imports ONLY the installed package, exactly as a
downstream user would after `pip install fds.protobuf.stach.extensions`.

Usage: python consume.py <resources_dir>
"""
import os
import sys

# Import the INSTALLED package (not the repo src/). If a declared dependency is
# missing from setup.py, this import is where it fails.
from fds.protobuf.stach.extensions.StachV3ExtensionFactory import (
    ConvertJsonToTable,
    ConvertArrowFileToTable,
    ConvertArrowStreamToTable,
)
import fds.protobuf.stach.extensions.StachV3ExtensionFactory as factory


def main(resources_dir):
    # Prove we are using the installed package, not the repo source.
    pkg_dir = os.path.dirname(factory.__file__)
    print(f"Imported package from: {pkg_dir}")
    assert "site-packages" in pkg_dir, (
        "Refusing to run: package was imported from the source tree, not an install. "
        "Run this from a clean venv with the wheel installed."
    )

    json_path = os.path.join(resources_dir, "V3JsonStachResponse.json")
    arrow_file_path = os.path.join(resources_dir, "V3ArrowFileStachResponse.arrow")
    arrow_stream_path = os.path.join(resources_dir, "V3ArrowStreamStachResponse.arrow")

    # 1) ConvertJsonToTable  (new signature: accepts JSON *content*, not a path)
    with open(json_path, "r") as f:
        json_content = f.read()
    json_result = ConvertJsonToTable(json_content)
    _check("ConvertJsonToTable", json_result)

    # 2) ConvertArrowFileToTable
    arrow_file_result = ConvertArrowFileToTable(arrow_file_path)
    _check("ConvertArrowFileToTable", arrow_file_result)

    # 3) ConvertArrowStreamToTable
    with open(arrow_stream_path, "rb") as f:
        arrow_bytes = f.read()
    arrow_stream_result = ConvertArrowStreamToTable(arrow_bytes)
    _check("ConvertArrowStreamToTable", arrow_stream_result)

    print("\nEND-USER TEST PASSED")


def _check(name, result):
    assert result is not None, f"{name}: result was None"
    assert "table" in result, f"{name}: missing 'table' key"
    table = result["table"]
    print(f"\n[{name}] shape={table.shape}, columns={list(table.columns)[:4]}...")
    print(table.head(2).to_string())


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python consume.py <resources_dir>", file=sys.stderr)
        sys.exit(2)
    main(sys.argv[1])
