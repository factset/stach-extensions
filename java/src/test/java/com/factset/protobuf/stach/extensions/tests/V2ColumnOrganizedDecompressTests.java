package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.extensions.v2.ColumnOrganizedStachDecompress;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONArray;

public class V2ColumnOrganizedDecompressTests {

    String stachDataJsonStr;
    String fileName = "V2ColumnOrganizedCompressed.json";
    List<String> expectedValues = Stream.of(
            null, null, null, "Americas",
            "Asia Pacific", "Europe", "Middle East and Africa",
            null, null, null, null,
            null, null, null, null)
    .collect(Collectors.toList());

    @BeforeTest
    public void setup() throws IOException {
        Path workingDirectory = Paths.get("src", "test", "java", "resources");

        stachDataJsonStr = new String(Files.readAllBytes(Paths.get(workingDirectory.toString(), fileName)));
    }

    @Test
    public void testGetPrimaryTableIds() {
        List<String> ids = ColumnOrganizedStachDecompress.getPrimaryTableIds(stachDataJsonStr);

        Assert.assertEquals(ids.size(), 1);
        Assert.assertEquals(ids.get(0), "a649ec50-7e58-443d-b791-1340e9eebf24");
    }

    @Test
    public void testDecompress() {

        String decompressedString = ColumnOrganizedStachDecompress.decompress(stachDataJsonStr);

        JSONObject decompressed = new JSONObject(decompressedString);
        String firstDataColumnId = "1";
        String primaryTableId = "a649ec50-7e58-443d-b791-1340e9eebf24";
        JSONArray values = decompressed
            .getJSONObject("tables").getJSONObject(primaryTableId)
            .getJSONObject("data").getJSONObject("columns").getJSONObject(firstDataColumnId)
            .getJSONArray("values");

        Assert.assertEquals(expectedValues.size(), values.length());

        for(int i=0; i < expectedValues.size(); i++)
        {   
            if (values.isNull(i)) {
                Assert.assertEquals(expectedValues.get(i), null);
            } else {
                Assert.assertEquals(expectedValues.get(i), values.getString(i));
            }
        }
    }
}