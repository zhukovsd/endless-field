package com.zhukovsd.endlessfield.field;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public class CellPosition {
    static int hashCodeFactor = 2539;

    public final int row, column;

    public CellPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CellPosition that = (CellPosition) o;

        if (row != that.row) return false;
        return column == that.column;

    }

    @Override
    public int hashCode() {
        int result = row;
        result = hashCodeFactor * result + column;
        return result;
    }
}
