package com.factset.protobuf.stach.extensions.v1;

import com.factset.protobuf.stach.MetadataItemProto.MetadataItem;
import com.factset.protobuf.stach.NullValues;
import com.factset.protobuf.stach.PackageProto.Package;
import com.factset.protobuf.stach.table.DataTypeProto.DataType;
import com.factset.protobuf.stach.table.SeriesDataProto.SeriesData;
import com.factset.protobuf.stach.table.SeriesDefinitionProto;
import com.factset.protobuf.stach.table.SeriesDefinitionProto.SeriesDefinition;
import com.factset.protobuf.stach.table.TableProto.Table;
import com.factset.protobuf.stach.extensions.models.Row;
import com.factset.protobuf.stach.extensions.models.TableData;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColumnOrganizedStachExtension {
    private static class SeriesDataHelper {
        /**
         * The purpose of this function is to return the value from the provided SeriesData object.
         * @param seriesData : The data of a series. A series is how a column of data is represented.
         * @param dataType   : The type of data in a series.
         * @param index      : index can be referred as a key which is used to access the value inside list.
         * @param nullFormat : nullFormat is used to render a null value with a special string, like -- or @NA.
         * @return Returns data object from the SeriesData.
         */
        private static Object getValueHelper(SeriesData seriesData, DataType dataType, int index,
                                             String nullFormat) {
            if (dataType == DataType.STRING) {
                String value = seriesData.getStringArray().getValues(index);
                return NullValues.STRING.equals(value) ? nullFormat : value;
            } else if (dataType == DataType.DOUBLE) {
                double value = seriesData.getDoubleArray().getValues(index);
                return Double.isNaN(value) ? nullFormat : value;
            } else if (dataType == DataType.BOOL) {
                return seriesData.getBoolArray().getValues(index);
            } else if (dataType == DataType.DURATION) {
                Duration value = seriesData.getDurationArray().getValues(index);
                return NullValues.DURATION.equals(value) ? nullFormat : value;
            } else if (dataType == DataType.FLOAT) {
                float value = seriesData.getFloatArray().getValues(index);
                return Float.isNaN(value) ? nullFormat : value;
            } else if (dataType == DataType.INT32) {
                int value = seriesData.getInt32Array().getValues(index);
                return NullValues.INT32 == value ? nullFormat : value;
            } else if (dataType == DataType.INT64) {
                long value = seriesData.getInt64Array().getValues(index);
                return NullValues.INT64 == value ? nullFormat : value;
            } else if (dataType == DataType.TIMESTAMP) {
                Timestamp value = seriesData.getTimestampArray().getValues(index);
                return NullValues.TIMESTAMP.equals(value) ? nullFormat : value;
            } else {
                throw new NotImplementedException(dataType + " is not implemented");
            }
        }
    }


    /**
     * The purpose of this function is to convert stach to Tabular format.
     * @param packageObj : Stach Data which is represented as a Package object.
     * @return Returns a list of tables for a given stach data.
     */
    public static List<TableData> convertToTableFormat(Package packageObj) {
        List<TableData> tables = new ArrayList<>();
        for (String primaryTableId : packageObj.getPrimaryTableIdsList()) {
            tables.add(generateTable(packageObj, primaryTableId));
        }
        return tables;
    }

    /**
     * The purpose of this function is to generate Table for a given table id in the provided stach data through the package.
     * @param packageObj     : Stach Data which is represented as a Package object.
     * @param primaryTableId : Refers to the id for a particular table inside a package.
     * @return Returns the generated Table from the package provided.
     */
    private static TableData generateTable(Package packageObj, String primaryTableId) {
        Map<String, Table> tablesMap = packageObj.getTablesMap();
        Table primaryTable = tablesMap.get(primaryTableId);
        String headerId = primaryTable.getDefinition().getHeaderTableId();
        Table headerTable = tablesMap.get(headerId);
        int rowsCount = primaryTable.getData().getRowsCount();

        TableData table = new TableData();

        List<SeriesDefinition> headerTableSeriesDefinitions = headerTable.getDefinition().getColumnsList();
        List<SeriesDefinition> primaryTableSeriesDefinitions = primaryTable.getDefinition().getColumnsList();

        Map<String, SeriesData> headerTableColumns = headerTable.getData().getColumnsMap();
        Map<String, SeriesData> primaryTableColumns = primaryTable.getData().getColumnsMap();

        for (SeriesDefinition headerTableseriesDefinition : headerTableSeriesDefinitions) {
            Row headerRow = new Row();
            headerRow.setHeader(true);

            for (SeriesDefinition primaryTableSeriesDefinition : primaryTableSeriesDefinitions) {
                if (primaryTableSeriesDefinition.getIsDimension()) {
                    headerRow.getCells().add(isNullOrEmpty(primaryTableSeriesDefinition.getDescription()) ?
                            primaryTableSeriesDefinition.getName() : primaryTableSeriesDefinition.getDescription());
                    continue;
                }

                String headerColumnId = primaryTableSeriesDefinition.getHeaderId();
                String nullFormat = headerTableseriesDefinition.getFormat().getNullFormat();

                int indexOfHeader = getIndexOf(headerTable.getData().getRowsList(), headerColumnId);
                headerRow.getCells().add(SeriesDataHelper.getValueHelper(headerTableColumns.get(headerTableseriesDefinition.getId()),
                        headerTableseriesDefinition.getType(), indexOfHeader, nullFormat).toString());
            }

            table.getRows().add(headerRow);
        }

        // Construct the column data
        for (int i = 0; i < rowsCount; i++) {
            Row dataRow = new Row();
            for (SeriesDefinitionProto.SeriesDefinition primaryTableSeriesDefinition : primaryTableSeriesDefinitions) {
                String nullFormat = primaryTableSeriesDefinition.getFormat().getNullFormat();
                String primaryTableColumnId = primaryTableSeriesDefinition.getId();
                dataRow.getCells().add(SeriesDataHelper.getValueHelper(primaryTableColumns.get(primaryTableColumnId),
                        primaryTableSeriesDefinition.getType(), i, nullFormat).toString());
            }
            table.getRows().add(dataRow);
        }

        Map<String, MetadataItem> metadata = primaryTable.getData().getMetadata().getItemsMap();
        for (Map.Entry<String, MetadataItem> entry : metadata.entrySet()) {
            table.getMetadata().put(entry.getValue().getName(), entry.getValue().getStringValue());
        }
        return table;
    }


    /**
     * The purpose of this function is to give the index of the given id in the list of SeriesDefinition.
     * @param list : List of SeriesDefinition objects.
     * @param id   : The id that is to be searched.
     * @return Returns the index of the SeriesDefinition with the given id.
     */
    private static int getIndexOf(List<SeriesDefinition> list, String id) {
        int pos = 0;

        for (SeriesDefinition myObj : list) {
            if (id.equalsIgnoreCase(myObj.getId()))
                return pos;
            pos++;
        }

        return -1;
    }

    /**
     * The purpose of this function is to check if the string is empty or is null.
     * @param str : The input string object.
     * @return Returns true if the string object is null or empty string.
     */
    private static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}