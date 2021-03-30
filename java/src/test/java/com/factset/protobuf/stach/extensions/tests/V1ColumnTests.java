package com.factset.protobuf.stach.extensions.tests;

import com.factset.protobuf.stach.PackageProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.factset.protobuf.stach.extensions.models.TableData;
import com.factset.protobuf.stach.extensions.v1.ColumnOrganizedStachExtension;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class V1ColumnTests {

    PackageProto.Package _package;

    List<String> row1 = Arrays.asList("total0","group1","group2", "Port.+Weight", "Bench.+Weight", "Difference");
    List<String> row2 = Arrays.asList("Total","","", "100.0", "--", "100.0");

    @BeforeClass
    public void setup() throws IOException {
        Path workingDirectory = Paths.get("src", "test", "java", "resources");
        String input = new String(Files.readAllBytes(Paths.get(workingDirectory.toString(), "V1Column.json")));
        PackageProto.Package.Builder builder = PackageProto.Package.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(input, builder);
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Error while deserializing the response");
            e.printStackTrace();
        }
        _package = builder.build();
    }

    @Test
    public void testConvert(){
        List<TableData> tableDataList = ColumnOrganizedStachExtension.convertToTableFormat(_package);

          Assert.assertEquals(row1, tableDataList.get(0).getRows().get(0).getCells());
        Assert.assertEquals(true, tableDataList.get(0).getRows().get(0).isHeader());

        Assert.assertEquals(row2, tableDataList.get(0).getRows().get(1).getCells());
        Assert.assertEquals(false, tableDataList.get(0).getRows().get(1).isHeader());

    }
}
