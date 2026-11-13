package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.extensions.StachV3ExtensionsFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class V3StachExtensionsTests {

    private StachV3ExtensionsFactory factory;
    private Path resourcesDirectory;

    private static final String FILE_V3_ARROW_FILE   = "V3ArrowFileStachResponse.arrow";
    private static final String FILE_V3_ARROW_STREAM = "V3ArrowStreamStachResponse.arrow";
    private static final String FILE_V3_JSON         = "V3JsonStachResponse.json";

    @BeforeTest
    public void setup() {
        factory = new StachV3ExtensionsFactory();
        resourcesDirectory = Paths.get("src", "test", "java", "Resources");
    }

    // ── ConvertArrowFileToTable ───────────────────────────────────────────────

    @Test
    public void testConvertArrowFileToTable_arrowFile_returnsNonEmptyList() {
        String filePath = Paths.get(resourcesDirectory.toString(), FILE_V3_ARROW_FILE).toString();

        List<List<Map<String, String>>> result = factory.ConvertArrowFileToTable(filePath);

        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertFalse(result.isEmpty(), "Result should contain at least one row");

        List<Map<String, String>> table = result.get(0);
        Assert.assertNotNull(table, "First table should not be null");
        Assert.assertFalse(table.isEmpty(), "First table should contain at least one row");

        System.out.println("Table count: " + result.size());
        System.out.println("Row count: " + table.size());
        if (!table.isEmpty()) {
            System.out.println("Columns: " + table.get(0).keySet());
        }
        for (int i = 0; i < table.size(); i++) {
            System.out.println("Row " + i + ": " + table.get(i));
        }
    }

    @Test
    public void testConvertArrowFileToTable_nullPath_returnsNull() {
        List<List<Map<String, String>>> result = factory.ConvertArrowFileToTable(null);
        Assert.assertNull(result);
    }

    @Test
    public void testConvertArrowFileToTable_emptyPath_returnsNull() {
        List<List<Map<String, String>>> result = factory.ConvertArrowFileToTable("");
        Assert.assertNull(result);
    }

    // ── ConvertArrowStreamToTable ─────────────────────────────────────────────

    @Test
    public void testConvertArrowStreamToTable_arrowStreamBytes_returnsNonEmptyList() throws IOException {
        byte[] arrowBytes = Files.readAllBytes(
                Paths.get(resourcesDirectory.toString(), FILE_V3_ARROW_STREAM));

        List<List<Map<String, String>>> result = factory.ConvertArrowStreamToTable(arrowBytes);

        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertFalse(result.isEmpty(), "Result should contain at least one row");

        List<Map<String, String>> table = result.get(0);
        Assert.assertNotNull(table, "First table should not be null");
        Assert.assertFalse(table.isEmpty(), "First table should contain at least one row");

        System.out.println("Table count: " + result.size());
        System.out.println("Row count: " + table.size());
        if (!table.isEmpty()) {
            System.out.println("Columns: " + table.get(0).keySet());
        }
        for (int i = 0; i < table.size(); i++) {
            System.out.println("Row " + i + ": " + table.get(i));
        }
    }

    @Test
    public void testConvertArrowStreamToTable_nullBytes_returnsNull() {
        List<List<Map<String, String>>> result = factory.ConvertArrowStreamToTable(null);
        Assert.assertNull(result);
    }

    @Test
    public void testConvertArrowStreamToTable_emptyBytes_returnsNull() {
        List<List<Map<String, String>>> result = factory.ConvertArrowStreamToTable(new byte[0]);
        Assert.assertNull(result);
    }

    // ── ConvertJsonToTable ────────────────────────────────────────────────────

    @Test
    public void testConvertJsonToTable_jsonFile_returnsNonEmptyDataSet() throws Exception {
        String fileContent = new String(Files.readAllBytes(
                Paths.get(resourcesDirectory.toString(), FILE_V3_JSON)));

        List<List<Map<String, String>>> result = factory.ConvertJsonToTable(fileContent);

        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertFalse(result.isEmpty(), "DataSet should contain at least one table");

        List<Map<String, String>> table = result.get(0);
        Assert.assertNotNull(table, "First table should not be null");
        Assert.assertFalse(table.isEmpty(), "First table should contain at least one row");

        System.out.println("Table count: " + result.size());
        System.out.println("Row count: " + table.size());
        if (!table.isEmpty()) {
            System.out.println("Columns: " + table.get(0).keySet());
        }
        for (int i = 0; i < table.size(); i++) {
            System.out.println("Row " + i + ": " + table.get(i));
        }
    }

    @Test
    public void testConvertJsonToTable_nullContent_throwsException() {
        try {
            factory.ConvertJsonToTable(null);
            Assert.fail("Expected exception for null input");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testConvertJsonToTable_emptyContent_throwsException() {
        try {
            factory.ConvertJsonToTable("");
            Assert.fail("Expected exception for empty input");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }
}