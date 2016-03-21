package com.zhukovsd.endlessfield.field;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public class ChunkSize {
    public int rowCount, columnCount;

    public ChunkSize(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public int cellCount() {
        return rowCount * columnCount;
    }
}
