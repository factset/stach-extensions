package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.extensions.StachV3ExtensionsFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class V3MultiLevelHeaderTests {

    private static final Path RESOURCES = Paths.get("src", "test", "java", "Resources");
    private static final Path ARROW_STREAM_FILE = RESOURCES.resolve("V3MultiLevelHeaderArrowStreamResponse.arrow");
    private static final Path JSON_FILE = RESOURCES.resolve("V3MultiLevelHeaderJsonResponse.json");

    private static final int EXPECTED_ROW_COUNT = 9521;
    private static final Set<String> EXPECTED_HEADERS = new LinkedHashSet<>(Arrays.asList(
            "group0", "group1", "group2", "group3", "group4", "group5",
            "Security Name",
            "26-OCT-2026 | Port. Weight",
            "26-OCT-2026 | Bench Weight",
            "26-OCT-2026 | Variation in Ending Weight",
            "3-string",
            "27-OCT-2026 | Port. Weight",
            "27-OCT-2026 | Bench Weight",
            "27-OCT-2026 | Variation in Ending Weight",
            "6-string"));

    private final StachV3ExtensionsFactory factory = new StachV3ExtensionsFactory();

    // Collapse line breaks and repeated spaces, so "Port.\nWeight" compares equal to "Port. Weight".
    static String normalizeHeader(String header) {
        return header.trim().replaceAll("\\s+", " ");
    }

    // Column names are the keys of each row map. Row maps are unordered, so headers are compared as a set.
    static Set<String> headersOf(List<List<Map<String, String>>> result) {
        Set<String> headers = new LinkedHashSet<>();
        if (result == null || result.isEmpty()) {
            return headers;
        }
        for (Map<String, String> row : result.get(0)) {
            for (String key : row.keySet()) {
                headers.add(normalizeHeader(key));
            }
        }
        return headers;
    }

    static int rowCountOf(List<List<Map<String, String>>> result) {
        return result.get(0).size();
    }

    @Test
    public void arrowStream_headersMatchExpected() throws Exception {
        List<List<Map<String, String>>> result =
                factory.ConvertArrowStreamToTable(Files.readAllBytes(ARROW_STREAM_FILE));
        Assert.assertEquals(headersOf(result), EXPECTED_HEADERS);
    }

    @Test
    public void arrowStream_rowCountMatchesExpected() throws Exception {
        List<List<Map<String, String>>> result =
                factory.ConvertArrowStreamToTable(Files.readAllBytes(ARROW_STREAM_FILE));
        Assert.assertEquals(rowCountOf(result), EXPECTED_ROW_COUNT);
    }

    @Test
    public void arrowFilePath_headersMatchExpected() {
        List<List<Map<String, String>>> result = factory.ConvertArrowFileToTable(ARROW_STREAM_FILE.toString());
        Assert.assertEquals(headersOf(result), EXPECTED_HEADERS);
    }

    @Test
    public void json_headersMatchExpected() throws Exception {
        String content = new String(Files.readAllBytes(JSON_FILE), StandardCharsets.UTF_8);
        List<List<Map<String, String>>> result;
        try {
            result = factory.ConvertJsonToTable(content);
        } catch (RuntimeException e) {
            Assert.fail("V3MultiLevelHeaderJsonResponse.json could not be converted (incomplete or invalid JSON?): " + e);
            return;
        }
        Set<String> headers = (result == null || result.isEmpty()) ? Collections.emptySet() : headersOf(result);
        if (headers.isEmpty()) {
            // The converter reports parse errors by returning no rows, so an empty result means the JSON was not converted.
            Assert.fail("V3MultiLevelHeaderJsonResponse.json produced no table (incomplete or invalid JSON?)");
        }
        Assert.assertEquals(headers, EXPECTED_HEADERS);
    }

    @Test
    public void arrowAndJson_headersMatch() throws Exception {
        Set<String> arrowHeaders = headersOf(
                factory.ConvertArrowStreamToTable(Files.readAllBytes(ARROW_STREAM_FILE)));
        String content = new String(Files.readAllBytes(JSON_FILE), StandardCharsets.UTF_8);
        Set<String> jsonHeaders = headersOf(factory.ConvertJsonToTable(content));
        Assert.assertEquals(arrowHeaders, jsonHeaders);
    }
}
