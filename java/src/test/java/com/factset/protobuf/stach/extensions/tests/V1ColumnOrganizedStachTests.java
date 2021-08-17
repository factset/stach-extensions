package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.extensions.ColumnStachExtensionBuilder;
import com.factset.protobuf.stach.extensions.StachExtensionFactory;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.models.TableData;
import com.google.protobuf.InvalidProtocolBufferException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class V1ColumnOrganizedStachTests {

    Path workingDirectory;
    ColumnStachExtensionBuilder stachExtensionBuilder;
    String fileV1ColumnOrganizedStach = "V1ColumnOrganizedStachData.json";
    String input;

    List<String> row1 = Arrays.asList("total0", "group1", "group2", "Port.+Weight", "Bench.+Weight", "Difference");
    List<String> row2 = Arrays.asList("Total", "", "", "100.0", "--", "100.0");

    private void readFile(String fileName){
        try{
            input = new String(Files.readAllBytes(Paths.get(workingDirectory.toString(), fileName)));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @BeforeClass
    public void setup() throws IOException {
        workingDirectory = Paths.get("src", "test", "java", "resources");
    }

    @Test
    public void testConvert() throws InvalidProtocolBufferException {
        readFile(fileV1ColumnOrganizedStach);

        stachExtensionBuilder = StachExtensionFactory.getColumnOrganizedBuilder(StachVersion.V1);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(row1, tableDataList.get(0).getRows().get(0).getCells());
        Assert.assertEquals(true, tableDataList.get(0).getRows().get(0).isHeader());

        Assert.assertEquals(row2, tableDataList.get(0).getRows().get(1).getCells());
        Assert.assertEquals(false, tableDataList.get(0).getRows().get(1).isHeader());

    }

    @Test
    public void testMetaData() throws InvalidProtocolBufferException {
        readFile(fileV1ColumnOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getColumnOrganizedBuilder(StachVersion.V1);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(tableDataList.get(0).getMetadata().keySet().toArray().length, 18);
        Assert.assertEquals(tableDataList.get(0).getMetadata().get("Report Frequency"), "Single");

    }

    @Test
    public void testMetaDataArray() throws InvalidProtocolBufferException {
        readFile(fileV1ColumnOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getColumnOrganizedBuilder(StachVersion.V1);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(tableDataList.get(0).getMetadataArray().keySet().toArray().length, 18);
        Assert.assertEquals(tableDataList.get(0).getMetadataArray().get("Grouping Frequency").get(0), "Economic Sector - Beginning of Period");


//        Assert.assertEquals(tableDataList.get(0).getRawMetadata().keySet().toArray().length, 18);
//        Assert.assertEquals(tableDataList.get(0).getRawMetadata().get("Grouping Frequency").getListValue().getValues(0).getStringValue(), "Economic Sector - Beginning of Period");
    }


}
