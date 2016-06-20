/*
 * Copyright 2016 Zhukov Sergei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhukovsd.endlessfield;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public class CellPosition {
    private static int hashCodeFactor = 2539;

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
}
