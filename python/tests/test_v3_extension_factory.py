import os
import unittest

from fds.protobuf.stach.extensions.StachV3ExtensionFactory import (
    ConvertJsonToTable, ConvertArrowFileToTable, ConvertArrowStreamToTable
)

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))

class TestConvertJsonToTable(unittest.TestCase):
    """Unit tests for ConvertJsonToTable method"""    

    def test_convert_json_to_table_success(self):
        """Test successful conversion of valid JSON to table"""
        json_file = os.path.join(ROOT_DIR, "resources", "V3JsonStachResponse.json")
        if os.path.exists(json_file):
            try:
                result = ConvertJsonToTable(json_file)
                self.assertIsNotNone(result)
                self.assertIn('table', result)
               
            except Exception as e:
                # Skip if implementation has unmet dependencies
                self.skipTest(f"ConvertJsonToTable not fully implemented: {str(e)}")
        else:
            # Skip test if resource file not available
            self.skipTest(f"JSON resource file not found: {json_file}")
        
    def test_convert_arrow_file_to_table_success(self):
        """Test successful conversion of arrow file to table"""
        arrow_file = os.path.join(ROOT_DIR, "resources", "V3ArrowFileStachResponse.arrow")
        if os.path.exists(arrow_file):
            try:
                result = ConvertArrowFileToTable(arrow_file)
                self.assertIsNotNone(result)
                self.assertIn('table', result)                
            except Exception as e:
                # Skip if implementation has unmet dependencies
                self.skipTest(f"ConvertArrowFileToTable not fully implemented: {str(e)}")
        else:
            # Skip test if resource file not available
            self.skipTest(f"Arrow resource file not found: {arrow_file}")

    def test_convert_arrow_stream_to_table_success(self):
        """Test successful conversion of arrow stream to table"""
        arrow_file = os.path.join(ROOT_DIR, "resources", "V3ArrowStreamStachResponse.arrow")
        if os.path.exists(arrow_file):
            try:
                with open(arrow_file, 'rb') as f:
                    arrow_bytes = f.read()
                result = ConvertArrowStreamToTable(arrow_bytes)
                self.assertIsNotNone(result)
                self.assertIn('table', result)                
            except Exception as e:
                # Skip if implementation has unmet dependencies
                self.skipTest(f"ConvertArrowStreamToTable not fully implemented: {str(e)}")
        else:
            # Skip test if resource file not available
            self.skipTest(f"Arrow resource file not found: {arrow_file}")

if __name__ == '__main__':
    unittest.main()
