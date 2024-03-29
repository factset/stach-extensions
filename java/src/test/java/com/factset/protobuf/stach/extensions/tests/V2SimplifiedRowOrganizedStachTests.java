package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.extensions.Configurations;
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

public class V2SimplifiedRowOrganizedStachTests {

    Path workingDirectory;
    RowStachExtensionBuilder stachExtensionBuilder;
    String fileV2SimplifiedRowOrganizedStach = "V2SimplifiedRowOrganizedStachData.json";
    String input;

    List<String> row1 = Arrays.asList("total0", "group1", "group2", "Port.+Weight", "Bench.+Weight", "Difference");
    List<String> row2 = Arrays.asList("Total", "", "", "100.0", "", "100.0");
    List<String> row3 = Arrays.asList("Communications", "", "", "13644858.4156231", "", "3.28747887030914");
    String inputFileName = "V2SimplifiedRowOrganizedStachData.json";

    private void readFile(String fileName){
        try{
            input = new String(Files.readAllBytes(Paths.get(workingDirectory.toString(), fileName)));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @BeforeTest
    public void setup() throws IOException {
        workingDirectory = Paths.get("src", "test", "java", "Resources");
    }

    @Test
    public void testConvert() throws InvalidProtocolBufferException {

        readFile(fileV2SimplifiedRowOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        Configurations.setSuppressScientificNotationForDoubles(false);
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(row1, tableDataList.get(0).getRows().get(0).getCells());
        Assert.assertEquals(true, tableDataList.get(0).getRows().get(0).isHeader());

        Assert.assertEquals(row2, tableDataList.get(0).getRows().get(1).getCells());
        Assert.assertEquals(false, tableDataList.get(0).getRows().get(1).isHeader());
        Assert.assertTrue(tableDataList.get(0).getRows().get(2).getCells().get(3).contains("E"));

        Configurations.setSuppressScientificNotationForDoubles(true); // resetting the flag to default value for other tests to work as expected

    }

    @Test
    public void testMetaData() throws InvalidProtocolBufferException {

        readFile(fileV2SimplifiedRowOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(tableDataList.get(0).getMetadata().keySet().toArray().length, 18);
        Assert.assertEquals(tableDataList.get(0).getMetadata().get("Report Frequency"), "[\"Single\"]");
    }

    @Test
    public void testMetaDataArray() throws InvalidProtocolBufferException {

        readFile(fileV2SimplifiedRowOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();


        Assert.assertEquals(tableDataList.get(0).getRawMetadata().keySet().toArray().length, 18);
        Assert.assertEquals(tableDataList.get(0).getRawMetadata().get("Report Frequency").get(0).getStringValue(), "Single");
        Assert.assertEquals(tableDataList.get(0).getRawMetadata().get("Grouping Frequency").get(1).getStringValue(), "Industry - Beginning of Period");
    }

    @Test
    public void testConvertWithOutScientificNotation() throws InvalidProtocolBufferException {
        readFile(fileV2SimplifiedRowOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(row3, tableDataList.get(0).getRows().get(2).getCells());
        Assert.assertEquals(false, tableDataList.get(0).getRows().get(2).isHeader());
    }
}
