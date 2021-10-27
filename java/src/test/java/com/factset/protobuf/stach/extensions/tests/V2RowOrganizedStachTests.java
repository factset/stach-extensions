package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.extensions.RowStachExtensionBuilder;
import com.factset.protobuf.stach.extensions.StachExtensionFactory;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.models.TableData;
import com.google.protobuf.InvalidProtocolBufferException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class V2RowOrganizedStachTests {

    Path workingDirectory;
    RowStachExtensionBuilder stachExtensionBuilder;
    String fileV2RowOrganizedStach = "V2RowOrganizedStachData.json";
    String fileV2RowOrganizedStachTable = "V2RowOrganizedStachTable.json";
    String input;

    List<String> row1 = Arrays.asList("total0", "group1", "group2", "Difference");
    List<String> row2 = Arrays.asList("Total", "", "", "--");
    List<String> row3 = Arrays.asList("Commercial Services", "", "", "0.913395362480243");

    private String readFile(String fileName){
        try{
            return new String(Files.readAllBytes(Paths.get(workingDirectory.toString(), fileName)));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @BeforeTest
    public void setup() throws IOException {
        workingDirectory = Paths.get("src", "test", "java", "resources");
        input = new String(Files.readAllBytes(Paths.get(workingDirectory.toString(), "V2RowOrganizedStachData.json")));
    }

    @Test
    public void testConvert() throws InvalidProtocolBufferException {
        input = readFile(fileV2RowOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(row1, tableDataList.get(0).getRows().get(0).getCells());
        Assert.assertTrue(tableDataList.get(0).getRows().get(0).isHeader());

        Assert.assertEquals(row2, tableDataList.get(0).getRows().get(1).getCells());
        Assert.assertFalse(tableDataList.get(0).getRows().get(1).isHeader());

        Assert.assertEquals(row3, tableDataList.get(0).getRows().get(2).getCells());
        Assert.assertFalse(tableDataList.get(0).getRows().get(1).isHeader());

    }

    @Test
    public void testConvertAddTable() throws InvalidProtocolBufferException {
        input = readFile(fileV2RowOrganizedStachTable);
        String stachInput = readFile(fileV2RowOrganizedStach);

        stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(stachInput).addTable("tableid0", input).
                addTable("tableId1", input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(3, tableDataList.size());

        Assert.assertEquals(row1, tableDataList.get(0).getRows().get(0).getCells());
        Assert.assertTrue(tableDataList.get(0).getRows().get(0).isHeader());

        Assert.assertEquals(row2, tableDataList.get(0).getRows().get(1).getCells());
        Assert.assertFalse(tableDataList.get(0).getRows().get(1).isHeader());

        Assert.assertEquals(row3, tableDataList.get(0).getRows().get(2).getCells());
        Assert.assertFalse(tableDataList.get(0).getRows().get(1).isHeader());

    }

    @Test
    public void testMetaData() throws InvalidProtocolBufferException {
        input = readFile(fileV2RowOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(tableDataList.get(0).getMetadata().keySet().toArray().length, 18);
        Assert.assertEquals(tableDataList.get(0).getMetadata().get("Report Frequency"), "[\"Single\"]");

    }

    @Test
    public void testMetaDataArray() throws InvalidProtocolBufferException {
        input = readFile(fileV2RowOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(tableDataList.get(0).getRawMetadata().keySet().toArray().length, 18);
        Assert.assertEquals(tableDataList.get(0).getRawMetadata().get("Report Frequency").get(0).getStringValue(), "Single");
        Assert.assertEquals(tableDataList.get(0).getRawMetadata().get("Grouping Frequency").get(1).getStringValue(), "Industry - FactSet - Beginning of Period");
    }
}
