package com.factset.protobuf.stach.extensions.models;

import com.google.protobuf.Value;

public class RowSpanSpread {

    private int position;
    private int rowspan;
    private int colspan;
    private Value value;

    public RowSpanSpread(int position, int rowspan, int colspan, Value value) {
        this.position = position;
        this.rowspan = rowspan;
        this.colspan = colspan;
        this.value = value;
    }

    public int getPosition() {
        return position;
    }

    public int getRowspan() {
        return rowspan;
    }

    public int getColspan() {
        return colspan;
    }

    public Value getValue() {
        return value;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
