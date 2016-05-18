package com.zhukovsd.endlessfield.field;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public class CellPosition {
    static int hashCodeFactor = 2539;

    public int row, column;

    public CellPosition() {

    }

    public CellPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    int compare(CellPosition o2) {
        if (row > o2.row)
            return 1;
        else if (row < o2.row)
            return -1;
        else {
            if (column > o2.column)
                return 1;
            else if (column < o2.column)
                return -1;
            else
                return 0;
        }
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(Integer.toString(row));
        builder.append(", ");
        builder.append(column);

        return builder.toString();
    }

    public static void main(String[] args) {
        long time = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            for (int row = 0; row < 50; row++) {
                for (int column = 0; column < 50; column++) {
                    CellPosition position = new CellPosition(row, column);
                }
            }
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("time = " + time + "ms");

        time = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            CellPosition position = new CellPosition(0, 0);
            for (int row = 0; row < 50; row++) {
                for (int column = 0; column < 50; column++) {
                    position.row = row;
                    position.column = column;
                }
            }
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("time = " + time + "ms");
    }
}
