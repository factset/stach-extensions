package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.extensions.StachExtensionBuilder;
import com.factset.protobuf.stach.extensions.StachExtensionFactory;
import com.factset.protobuf.stach.extensions.StachExtensions;
import com.factset.protobuf.stach.extensions.models.StachVersion;
import com.factset.protobuf.stach.extensions.models.TableData;
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

    StachExtensionBuilder stachExtensionBuilder;
    String input;

    List<String> row1 = Arrays.asList("total0","group1","group2", "Port.+Weight", "Bench.+Weight", "Difference");
    List<String> row2 = Arrays.asList("Total","","", "100.0", "--", "100.0");

    @BeforeClass
    public void setup() throws IOException {
        Path workingDirectory = Paths.get("src", "test", "java", "resources");
        input = new String(Files.readAllBytes(Paths.get(workingDirectory.toString(), "V1ColumnOrganizedStachData.json")));
    }

    @Test
    public void testConvert(){

        stachExtensionBuilder =  StachExtensionFactory.getBuilder(StachVersion.V1, null);
        StachExtensions stachExtension = stachExtensionBuilder.set(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(row1, tableDataList.get(0).getRows().get(0).getCells());
        Assert.assertEquals(true, tableDataList.get(0).getRows().get(0).isHeader());

        Assert.assertEquals(row2, tableDataList.get(0).getRows().get(1).getCells());
        Assert.assertEquals(false, tableDataList.get(0).getRows().get(1).isHeader());

    }

    @Test
    public void testMetaData() {

        stachExtensionBuilder =  StachExtensionFactory.getBuilder(StachVersion.V1, null);
        StachExtensions stachExtension = stachExtensionBuilder.set(input).build();
        List<TableData> tableDataList = stachExtension.convertToTable();

        Assert.assertEquals(tableDataList.get(0).getMetadata().keySet().toArray().length, 18);
        Assert.assertEquals(tableDataList.get(0).getMetadata().get("Report Frequency"),"Single");

    }
}
