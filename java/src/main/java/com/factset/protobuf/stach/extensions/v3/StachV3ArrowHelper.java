package com.factset.protobuf.stach.extensions.v3;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.ArrowStreamReader;
import org.apache.arrow.vector.ipc.SeekableReadChannel;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.arrow.vector.util.ByteArrayReadableSeekableByteChannel;

import com.factset.protobuf.stach.v3.TableProto.Table;
import com.factset.protobuf.stach.v3.ViewsProto.Views;

public class StachV3ArrowHelper extends StachV3Constants {

    public ConversionContext arrowContext = new ConversionContext();

    // Get meta data details
    public void GetMetaData(Schema schema) throws InvalidProtocolBufferException {
        if (schema != null) {

            Map<String, String> metadata = schema.getCustomMetadata();

            if (metadata != null && !metadata.isEmpty()) {

                // reading table
                if (metadata.containsKey(ArrowSchemaMetadataNames.Table)) {
                    String tableObject = metadata.get(ArrowSchemaMetadataNames.Table);
                    byte[] tableArrayDecoded = Base64.getDecoder().decode(tableObject);
                    arrowContext.stachTable = Table.parseFrom(tableArrayDecoded);
                }

                // reading views
                if (metadata.containsKey(ArrowSchemaMetadataNames.Views)) {
                    String viewObject = metadata.get(ArrowSchemaMetadataNames.Views);
                    byte[] viewsArrayDecoded = Base64.getDecoder().decode(viewObject);
                    Views views = Views.parseFrom(viewsArrayDecoded);

                    if (views != null && views.getViewsCount() > 0) {
                        Views.View view = views.getViews(0);
                        if (view != null && view.hasTable() && view.getTable().getHeadersCount() > 0) {
                            arrowContext.viewHeaders = new HashMap<>();
                            for (Map.Entry<String, String> kvp : view.getTable().getHeadersMap().entrySet()) {
                                arrowContext.viewHeaders.put(kvp.getKey(), kvp.getValue());
                            }
                        } else {
                            arrowContext.viewHeaders = new HashMap<>();
                        }
                    } else {
                        arrowContext.viewHeaders = new HashMap<>();
                    }
                }

                // reading multilevel headers table stored in the metadata of the schema
                VectorSchemaRoot multiLevelHeaders = null;
                if (metadata.containsKey(ArrowSchemaMetadataNames.MultiLevelHeadersTable)) {
                    String multiLevelHeadersTableObject = metadata.get(ArrowSchemaMetadataNames.MultiLevelHeadersTable);
                    byte[] multiLevelHeadersArrayDecoded = Base64.getDecoder().decode(multiLevelHeadersTableObject);

                    if (multiLevelHeadersArrayDecoded != null && multiLevelHeadersArrayDecoded.length > 0) {
                        try {
                            multiLevelHeaders = generateMultiLevelHeaders(multiLevelHeadersArrayDecoded);

                            if (multiLevelHeaders != null) {
                                for (int rowIndex = 0; rowIndex < multiLevelHeaders.getRowCount(); rowIndex++) {
                                    String concatColumnValue = "";
                                    String primaryKeyColumn = "";

                                    for (FieldVector columnVector : multiLevelHeaders.getFieldVectors()) {
                                        String headerColumnValue = getColumnValue(columnVector, rowIndex);

                                        if (columnVector.getName().equals(arrowContext.stachTable.getSplitResult().getMultiLevelHeadersTableReference())) {
                                            primaryKeyColumn = headerColumnValue;
                                        }

                                        if (headerColumnValue != null && !headerColumnValue.isEmpty()) {
                                            if (arrowContext.viewHeaders != null && !arrowContext.viewHeaders.isEmpty()) {
                                                if (arrowContext.viewHeaders.containsKey(headerColumnValue)) {
                                                    headerColumnValue = arrowContext.viewHeaders.getOrDefault(headerColumnValue, headerColumnValue);
                                                }
                                            }
                                            concatColumnValue = headerColumnValue + columnSeparator + concatColumnValue;
                                        }
                                    }
                                    if (primaryKeyColumn != null && !primaryKeyColumn.isEmpty()) {
                                        arrowContext.concatColumnsList.put(primaryKeyColumn, concatColumnValue.replaceAll("\\s*\\|\\s*$", ""));
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Error processing multi-level headers: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }

                // checking the column names against the view headers
                for (Field field : schema.getFields()) {
                    String headerName = field.getName();
                    if (!arrowContext.concatColumnsList.isEmpty()) {
                        headerName = arrowContext.concatColumnsList.getOrDefault(field.getName(), field.getName());
                    } else {
                        headerName = arrowContext.viewHeaders.getOrDefault(field.getName(), field.getName());
                    }
                    arrowContext.columnHeadersMapping.put(field.getName(), headerName);
                }
            }
        }
    }

    // Helper to generate multilevel headers
    public VectorSchemaRoot generateMultiLevelHeaders(byte[] multiLevelHeadersArrayDecoded) throws IOException {
        List<VectorSchemaRoot> multiLevelHeadersRecords = new ArrayList<>();
        VectorSchemaRoot mlhTable = null;
        BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE);

        if (IsArrowBytes(multiLevelHeadersArrayDecoded)) {
            try (ArrowFileReader arrowFileReader = new ArrowFileReader(
                    new SeekableReadChannel(new ByteArrayReadableSeekableByteChannel(multiLevelHeadersArrayDecoded)), allocator)) {

                Schema schema = arrowFileReader.getVectorSchemaRoot().getSchema();

                if (arrowFileReader.loadNextBatch()) {
                    VectorSchemaRoot root = arrowFileReader.getVectorSchemaRoot();
                    multiLevelHeadersRecords.add(root);
                }

                mlhTable = readMultiLevelHeadersSchema(multiLevelHeadersRecords, schema);
            }
        } else {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(multiLevelHeadersArrayDecoded);
                 ArrowStreamReader arrowStreamReader = new ArrowStreamReader(bais, allocator)) {

                Schema schema = arrowStreamReader.getVectorSchemaRoot().getSchema();

                if (arrowStreamReader.loadNextBatch()) {
                    VectorSchemaRoot root = arrowStreamReader.getVectorSchemaRoot();
                    multiLevelHeadersRecords.add(root);
                }

                mlhTable = readMultiLevelHeadersSchema(multiLevelHeadersRecords, schema);
            }
        }
        allocator.close();
        return mlhTable;
    }

    // Check if a file path points to an Arrow IPC file (vs stream)
    public boolean IsArrowFile(String filePath) throws IOException {
        FileInputStream fileStream = new FileInputStream(filePath);
        byte[] arrowFileMagic = new byte[]{'A', 'R', 'R', 'O', 'W', '1'};
        byte[] buffer = new byte[6];
        try (BufferedInputStream bufferedStream = new BufferedInputStream(fileStream)) {
            bufferedStream.mark(6);
            int read = bufferedStream.read(buffer, 0, 6);
            bufferedStream.reset();
            return read == 6 && Arrays.equals(buffer, arrowFileMagic);
        }
    }

    // Check if a byte array is an Arrow IPC file (vs stream)
    public boolean IsArrowBytes(byte[] arrowBytes) {
        byte[] arrowFileMagic = new byte[]{'A', 'R', 'R', 'O', 'W', '1'};
        if (arrowBytes.length < arrowFileMagic.length) {
            return false;
        }
        for (int i = 0; i < arrowFileMagic.length; i++) {
            if (arrowBytes[i] != arrowFileMagic[i]) {
                return false;
            }
        }
        return true;
    }

    // Get table data
    public void GetTableData(VectorSchemaRoot root) {

        int rowCount = root.getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Map<String, String> dataRow = new HashMap<>();

            String securityColumnKey = arrowContext.columnHeadersMapping.entrySet().stream()
                    .filter(e -> e.getValue().toLowerCase().contains(securityNameColumn.toLowerCase()))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
            FieldVector securityNameColumnData = securityColumnKey != null ? root.getVector(securityColumnKey) : null;

            String levelColumnKey = arrowContext.columnHeadersMapping.entrySet().stream()
                    .filter(e -> e.getKey().toLowerCase().contains(levelNameColumn.toLowerCase()))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
            FieldVector levelColumnData = levelColumnKey != null ? root.getVector(levelColumnKey) : null;

            int levelIndex = 0;
            if (levelColumnData != null) {
                String levelValue = getColumnValue(levelColumnData, rowIndex);
                levelIndex = levelValue != null && !levelValue.isEmpty() ? Integer.parseInt(levelValue) : 0;
            }

            for (Map.Entry<String, String> column : arrowContext.columnHeadersMapping.entrySet()) {
                if (!column.getKey().contains(groupPrefix)) {
                    FieldVector columnData = root.getVector(column.getKey());
                    String columnName = column.getValue();
                    if (columnData == null || columnName == null || columnName.isEmpty()) continue;
                    dataRow.put(columnName, getColumnValue(columnData, rowIndex));
                } else {
                    String columnName = column.getKey();
                    int groupIndex = Integer.parseInt(columnName.replace(groupPrefix, ""));
                    if (levelIndex == groupIndex) {
                        dataRow.put(columnName, securityNameColumnData != null ? getColumnValue(securityNameColumnData, rowIndex) : "");
                    } else if (rowIndex > 0 && levelIndex > 0 && groupIndex < levelIndex) {
                        dataRow.put(columnName, securityNameColumnData != null ? getColumnValue(securityNameColumnData, rowIndex - 1) : "");
                    }
                }
            }
            arrowContext.dataTable.add(dataRow);
        }
    }

    // Helper to read multilevel headers schema
    public VectorSchemaRoot readMultiLevelHeadersSchema(List<VectorSchemaRoot> multiLevelHeadersRecords, Schema schema) {
        BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE);
        int nColumns = schema.getFields().size();
        List<FieldVector> concatenatedVectors = new ArrayList<>();

        for (int colIdx = 0; colIdx < nColumns; colIdx++) {
            Field field = schema.getFields().get(colIdx);
            FieldVector mergedVector = field.createVector(allocator);
            mergedVector.allocateNew();
            int valueIndex = 0;
            for (VectorSchemaRoot batch : multiLevelHeadersRecords) {
                FieldVector batchVector = batch.getVector(colIdx);
                int batchRowCount = batchVector.getValueCount();
                for (int row = 0; row < batchRowCount; row++) {
                    if (batchVector.isNull(row)) {
                        mergedVector.setNull(valueIndex);
                    } else {
                        mergedVector.copyFromSafe(row, valueIndex, batchVector);
                    }
                    valueIndex++;
                }
            }
            mergedVector.setValueCount(valueIndex);
            concatenatedVectors.add(mergedVector);
        }
        return new VectorSchemaRoot(schema, concatenatedVectors, concatenatedVectors.get(0).getValueCount());
    }

    // Helper to get column value as string
    public String getColumnValue(FieldVector vector, int rowIndex) {
        if (vector == null) return "";
        Object value = vector.getObject(rowIndex);
        return value == null ? "" : value.toString();
    }

    // Helper to get levels from Arrow IPC stream file path
    public long[] getLevelsFromArrowStream(String filePath, BufferAllocator allocator) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ArrowStreamReader reader = new ArrowStreamReader(fis.getChannel(), allocator)) {
            VectorSchemaRoot root = reader.getVectorSchemaRoot();
            if (reader.loadNextBatch()) {
                FieldVector levelVector = root.getVector("level");
                if (levelVector != null) {
                    int valueCount = levelVector.getValueCount();
                    long[] levels = new long[valueCount];
                    for (int i = 0; i < valueCount; i++) {
                        Object value = levelVector.getObject(i);
                        levels[i] = value instanceof Number ? ((Number) value).longValue() : 0;
                    }
                    return levels;
                }
            }
        }
        return new long[0];
    }

    // Helper to get levels from Arrow IPC file path
    public long[] getLevelsFromArrowFile(String filePath, BufferAllocator allocator) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ArrowFileReader reader = new ArrowFileReader(new SeekableReadChannel(fis.getChannel()), allocator)) {
            VectorSchemaRoot root = reader.getVectorSchemaRoot();
            if (reader.loadNextBatch()) {
                FieldVector levelVector = root.getVector("level");
                if (levelVector != null) {
                    int valueCount = levelVector.getValueCount();
                    long[] levels = new long[valueCount];
                    for (int i = 0; i < valueCount; i++) {
                        Object value = levelVector.getObject(i);
                        levels[i] = value instanceof Number ? ((Number) value).longValue() : 0;
                    }
                    return levels;
                }
            }
        }
        return new long[0];
    }

    // Helper to get levels from Arrow IPC stream bytes
    public long[] getLevelsFromArrowStreamBytes(byte[] arrowBytes, BufferAllocator allocator) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(arrowBytes);
             ArrowStreamReader reader = new ArrowStreamReader(bais, allocator)) {
            VectorSchemaRoot root = reader.getVectorSchemaRoot();
            if (reader.loadNextBatch()) {
                FieldVector levelVector = root.getVector("level");
                if (levelVector != null) {
                    int valueCount = levelVector.getValueCount();
                    long[] levels = new long[valueCount];
                    for (int i = 0; i < valueCount; i++) {
                        Object value = levelVector.getObject(i);
                        levels[i] = value instanceof Number ? ((Number) value).longValue() : 0;
                    }
                    return levels;
                }
            }
        }
        return new long[0];
    }

    // Helper to get levels from Arrow IPC file bytes
    public long[] getLevelsFromArrowFileBytes(byte[] arrowBytes, BufferAllocator allocator) throws IOException {
        try (ArrowFileReader reader = new ArrowFileReader(
                new SeekableReadChannel(new ByteArrayReadableSeekableByteChannel(arrowBytes)), allocator)) {
            VectorSchemaRoot root = reader.getVectorSchemaRoot();
            if (reader.loadNextBatch()) {
                FieldVector levelVector = root.getVector("level");
                if (levelVector != null) {
                    int valueCount = levelVector.getValueCount();
                    long[] levels = new long[valueCount];
                    for (int i = 0; i < valueCount; i++) {
                        Object value = levelVector.getObject(i);
                        levels[i] = value instanceof Number ? ((Number) value).longValue() : 0;
                    }
                    return levels;
                }
            }
        }
        return new long[0];
    }
}