package com.zhukovsd;

/**
 * Created by ZhukovSD on 25.11.2015.
 */
public class FieldCell {
    int row, column;

    String text;
    boolean isChecked = false;

    public FieldCell(int row, int column, String text) {
        this.row = row;
        this.column = column;
        this.text = text;
    }
}
