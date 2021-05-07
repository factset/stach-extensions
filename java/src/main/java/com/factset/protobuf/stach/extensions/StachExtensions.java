package com.factset.protobuf.stach.extensions;

import com.factset.protobuf.stach.extensions.models.TableData;

import java.util.List;

public interface StachExtensions {

    /**
     * Converts the Package/RowOrganized Package to the list of TableData objects.
     * @return The list of converted TableData objects. The TableData contains the data rows along with metadata.
     */
    List<TableData> convertToTable();

}
