package com.factset.protobuf.stach.extensions.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONArray;

public class ColumnOrganizedStachDecompress {
    /**
     * Get a list of primary table ids from stach data
     *
     * @param pkg : The stach data.
     * @return Returns a list of primary table ids
    */
    public static List<String> getPrimaryTableIds(String pkg) {
        JSONObject pkgJson = new JSONObject(pkg);

        List<Object> ids = pkgJson.getJSONArray("primaryTableIds").toList();

        List<String> retList = new ArrayList<String>();
        ids.forEach(value -> retList.add((String)value));

        return retList;
    }

    /**
     * Decompress stach data. Goes through every primary table id.
     *
     * @param pkg : The stach data.
     * @return Returns a list of primary table ids
    */
    public static String decompress(String pkg) {
        Iterator<String> primaryTableIds = getPrimaryTableIds(pkg).iterator();
        JSONObject pkgJson = new JSONObject(pkg);

        while(primaryTableIds.hasNext()) {
            String tableId = primaryTableIds.next();
            
            decompress(pkgJson, tableId);
        }

        return pkgJson.toString();
    }

    private static JSONObject decompress(JSONObject pkg, String primaryTableId) {
        // Decompress the pages
        JSONObject columns = pkg.getJSONObject("tables").getJSONObject(primaryTableId).getJSONObject("data").getJSONObject("columns");
        Iterator<String> columnIds = columns.keySet().iterator();
        while (columnIds.hasNext()) {
            String columnId = columnIds.next();
            JSONObject column = columns.getJSONObject(columnId);
            if (!column.has("ranges")) {
                continue;
            }
            column = decompressColumn(column);
        }

        return pkg;
    }

    private static JSONObject decompressColumn(JSONObject column) {
        JSONObject ranges = column.getJSONObject("ranges");
        JSONArray compressedValues = column.getJSONArray("values");

        JSONArray decompressedValues = new JSONArray();
        int compressedLength = compressedValues.length();

        for(int compressedIndex = 0; compressedIndex < compressedLength; compressedIndex++) {
            String currentDecompressedIndex = Integer.toString(decompressedValues.length());

            if(!ranges.has(currentDecompressedIndex)) {
                Object obj = compressedValues.get(compressedIndex);
                decompressedValues.put(obj);
                continue;
            }

            int rangeValue = ranges.getInt(currentDecompressedIndex);
            for(int rangeIndex = 0; rangeIndex < rangeValue; rangeIndex++) {
                decompressedValues.put(compressedValues.get(compressedIndex));
            }
        }

        column.remove("ranges");
        column.put("values", decompressedValues);

        return column;
    }
}