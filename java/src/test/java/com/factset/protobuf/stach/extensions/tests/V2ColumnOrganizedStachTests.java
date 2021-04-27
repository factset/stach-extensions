package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.extensions.StachExtensionBuilderColumn;
import com.factset.protobuf.stach.extensions.StachExtensionFactory;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.models.TableData;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class V2ColumnOrganizedStachTests {

    Path workingDirectory;
    StachExtensionBuilderColumn stachExtensionBuilder;
    String fileV2ColumnOrganizedStach = "V2ColumnOrganizedStachData.json";
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

    @BeforeTest
    public void setup() throws IOException {
        workingDirectory = Paths.get("src", "test", "java", "resources");
    }

    @Test
    public void testConvert() {
        readFile(fileV2ColumnOrganizedStach);

        stachExtensionBuilder = StachExtensionFactory.getColumnOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(row1, tableDataList.get(0).getRows().get(0).getCells());
        Assert.assertEquals(true, tableDataList.get(0).getRows().get(0).isHeader());

        Assert.assertEquals(row2, tableDataList.get(0).getRows().get(1).getCells());
        Assert.assertEquals(false, tableDataList.get(0).getRows().get(1).isHeader());

    }

    @Test
    public void testMetaData() {
        readFile(fileV2ColumnOrganizedStach);
        stachExtensionBuilder = StachExtensionFactory.getColumnOrganizedBuilder(StachVersion.V2);
        StachExtensions stachExtension = stachExtensionBuilder.setPackage(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(tableDataList.get(0).getMetadata().keySet().toArray().length, 19);
        Assert.assertEquals(tableDataList.get(0).getMetadata().get("Report Frequency"), "Single");

    }

}
