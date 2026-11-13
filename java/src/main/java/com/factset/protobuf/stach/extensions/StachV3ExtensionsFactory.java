
package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.v3.StachV3ArrowHelper;
import com.factset.protobuf.stach.extensions.v3.StachV3JsonHelper;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.*;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.ArrowStreamReader;
import org.apache.arrow.vector.ipc.SeekableReadChannel;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.arrow.vector.util.ByteArrayReadableSeekableByteChannel;
import org.json.JSONObject;

public class StachV3ExtensionsFactory {

    private final StachV3JsonHelper jsonHelper = new StachV3JsonHelper();
    private final StachV3ArrowHelper arrowHelper = new StachV3ArrowHelper();

    public List<List<Map<String, String>>> ConvertArrowFileToTable(String filePath) {

        arrowHelper.arrowContext = new com.factset.protobuf.stach.extensions.v3.ConversionContext();

        try {

            if (filePath == null || filePath.isEmpty()) {
                System.out.println("File path is null or empty.");
                return null;
            }

            BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE);

            if (!arrowHelper.IsArrowFile(filePath)) {

                FileInputStream fileInputStream = new FileInputStream(filePath);
                ArrowStreamReader arrowStreamReader = new ArrowStreamReader(fileInputStream, allocator);

                VectorSchemaRoot schemaRoot = arrowStreamReader.getVectorSchemaRoot();
                Schema schema = arrowStreamReader.getVectorSchemaRoot().getSchema();

                long[] levels = arrowHelper.getLevelsFromArrowStream(filePath, allocator);
                if (levels != null && levels.length > 0) {
                    long maxLevel = Arrays.stream(levels).max().getAsLong();
                    for (int i = 0; i <= maxLevel; i++) {
                        String levelColumnName = arrowHelper.groupPrefix + i;
                        arrowHelper.arrowContext.columnHeadersMapping.putIfAbsent(levelColumnName, levelColumnName);
                    }
                }

                arrowHelper.GetMetaData(schema);

                while (arrowStreamReader.loadNextBatch()) {
                    arrowHelper.GetTableData(schemaRoot);
                }
            } else {

                FileInputStream fileInputStream = new FileInputStream(filePath);
                ArrowFileReader arrowFileReader = new ArrowFileReader(new SeekableReadChannel(fileInputStream.getChannel()), allocator);

                VectorSchemaRoot schemaRoot = arrowFileReader.getVectorSchemaRoot();
                Schema schema = arrowFileReader.getVectorSchemaRoot().getSchema();

                long[] levels = arrowHelper.getLevelsFromArrowFile(filePath, allocator);
                if (levels != null && levels.length > 0) {
                    long maxLevel = Arrays.stream(levels).max().getAsLong();
                    for (int i = 0; i <= maxLevel; i++) {
                        String levelColumnName = arrowHelper.groupPrefix + i;
                        arrowHelper.arrowContext.columnHeadersMapping.putIfAbsent(levelColumnName, levelColumnName);
                    }
                }

                arrowHelper.GetMetaData(schema);

                while (arrowFileReader.loadNextBatch()) {
                    arrowHelper.GetTableData(schemaRoot);
                }
            }

            for (String column : arrowHelper.ignoredColumns) {
                arrowHelper.arrowContext.dataTable.forEach(row -> row.remove(column));
            }
        } catch (Exception ex) {
            System.out.println("Exception occurred: " + ex.getMessage());
        }

        List<List<Map<String, String>>> dataSet = new ArrayList<>();
        dataSet.add(arrowHelper.arrowContext.dataTable);

        return dataSet;
    }

    public List<List<Map<String, String>>> ConvertArrowStreamToTable(byte[] arrowBytes) {

        arrowHelper.arrowContext = new com.factset.protobuf.stach.extensions.v3.ConversionContext();

        try {

            if (arrowBytes == null || arrowBytes.length == 0) {
                return null;
            }

            BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE);

            if (!arrowHelper.IsArrowBytes(arrowBytes)) {

                try (ByteArrayInputStream bais = new ByteArrayInputStream(arrowBytes);
                     ArrowStreamReader arrowStreamReader = new ArrowStreamReader(bais, allocator)) {

                    VectorSchemaRoot schemaRoot = arrowStreamReader.getVectorSchemaRoot();
                    Schema schema = arrowStreamReader.getVectorSchemaRoot().getSchema();

                    long[] levels = arrowHelper.getLevelsFromArrowStreamBytes(arrowBytes, allocator);
                    if (levels != null && levels.length > 0) {
                        long maxLevel = Arrays.stream(levels).max().getAsLong();
                        for (int i = 0; i <= maxLevel; i++) {
                            String levelColumnName = arrowHelper.groupPrefix + i;
                            arrowHelper.arrowContext.columnHeadersMapping.putIfAbsent(levelColumnName, levelColumnName);
                        }
                    }

                    arrowHelper.GetMetaData(schema);

                    while (arrowStreamReader.loadNextBatch()) {
                        arrowHelper.GetTableData(schemaRoot);
                    }
                }
            } else {

                try (ArrowFileReader arrowFileReader = new ArrowFileReader(
                        new SeekableReadChannel(new ByteArrayReadableSeekableByteChannel(arrowBytes)), allocator)) {

                    VectorSchemaRoot schemaRoot = arrowFileReader.getVectorSchemaRoot();
                    Schema schema = arrowFileReader.getVectorSchemaRoot().getSchema();

                    long[] levels = arrowHelper.getLevelsFromArrowFileBytes(arrowBytes, allocator);
                    if (levels != null && levels.length > 0) {
                        long maxLevel = Arrays.stream(levels).max().getAsLong();
                        for (int i = 0; i <= maxLevel; i++) {
                            String levelColumnName = arrowHelper.groupPrefix + i;
                            arrowHelper.arrowContext.columnHeadersMapping.putIfAbsent(levelColumnName, levelColumnName);
                        }
                    }

                    arrowHelper.GetMetaData(schema);

                    while (arrowFileReader.loadNextBatch()) {
                        arrowHelper.GetTableData(schemaRoot);
                    }
                }
            }

            for (String column : arrowHelper.ignoredColumns) {
                arrowHelper.arrowContext.dataTable.forEach(row -> row.remove(column));
            }
        } catch (Exception ex) {
            System.out.println("Exception occurred: " + ex.getMessage());
        }

        List<List<Map<String, String>>> dataSet = new ArrayList<>();
        dataSet.add(arrowHelper.arrowContext.dataTable);

        return dataSet;
    }

    public List<List<Map<String, String>>> ConvertJsonToTable(String fileContent) {
        jsonHelper.columnHeadersMapping = new LinkedHashMap<>();
        jsonHelper.dataTable = new ArrayList<>();
        jsonHelper.concatColumnsList = new LinkedHashMap<>();
        jsonHelper.jsonStachTable = new com.factset.protobuf.stach.extensions.v3.TableSchema();
        jsonHelper.customStachV3Table = new com.factset.protobuf.stach.extensions.v3.TableSchema();
        jsonHelper.customStachV3Views = new com.factset.protobuf.stach.extensions.v3.TableSchema();
        jsonHelper.jsonMultiLevelHeaders = new com.factset.protobuf.stach.extensions.v3.TableSchema();
        jsonHelper.dataSet = new ArrayList<>();

        if (fileContent == null || fileContent.isEmpty()) {
            throw new IllegalArgumentException("fileContent cannot be null or empty");
        }

        try {

            JSONObject stachJObject = new JSONObject(fileContent);

            jsonHelper.parseTableMetadata(stachJObject);
            jsonHelper.parseViews(stachJObject);
            jsonHelper.parseColumnsAndRows(stachJObject);
            jsonHelper.parseMultiLevelHeaders(stachJObject);
            jsonHelper.buildConcatColumnsList();
            jsonHelper.buildColumnHeadersMapping();
            jsonHelper.buildDataTable();

            for (String column : jsonHelper.ignoredColumns) {
                jsonHelper.dataTable.forEach(row -> row.remove(column));
            }
        } catch (Exception ex) {
            System.out.println("Exception occurred: " + ex.getMessage());
        }

        jsonHelper.dataSet.add(jsonHelper.dataTable);

        return jsonHelper.dataSet;
    }
}