package com.factset.protobuf.stach.extensions.models;

import java.util.ArrayList;
import java.util.List;

public class Row {
    private boolean isHeader;
    private List<String> Cells = new ArrayList<String>();

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public List<String> getCells() {
        return Cells;
    }

    public void setCells(List<String> cells) {
        this.Cells = cells;
    }
}
