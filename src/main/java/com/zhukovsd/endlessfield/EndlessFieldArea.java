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

import com.zhukovsd.endlessfield.field.EndlessField;

import java.util.Iterator;

/**
 * Created by ZhukovSD on 09.06.2016.
 */
public class EndlessFieldArea implements Iterable<CellPosition> {
    private final EndlessField<?> field;
    public CellPosition origin;
    public int rowCount, columnCount;

    public EndlessFieldArea(EndlessField<?> field, CellPosition origin, int rowCount, int columnCount) {
        // TODO: 28.06.2016 check constraints
        this.field = field;
        this.origin = origin;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public EndlessFieldArea expandFromCenter(int rowAmount, int columnAmount) {
        int leftBoundary = Math.max(origin.column - columnAmount, 0);
        int topBoundary = Math.max(origin.row - rowAmount, 0);

        int rightBoundary = Math.min(
                (origin.column + columnCount - 1) + columnAmount, field.sizeConstraints.maxColumn(field.chunkSize)
        );
        int bottomBoundary = Math.min(
                (origin.row + rowCount -1) + rowAmount, field.sizeConstraints.maxRow(field.chunkSize)
        );

        origin = new CellPosition(topBoundary, leftBoundary);

        rowCount = bottomBoundary - topBoundary + 1;
        columnCount = rightBoundary - leftBoundary + 1;

        return this;
    };

    public EndlessFieldArea expandFromCenter(int amount) {
        return expandFromCenter(amount, amount);
    }

    public EndlessFieldArea narrowToCenter(int amount) {
        if ((amount * 2 + 1 <= columnCount) && (amount * 2 + 1 <= rowCount)) {
            return this.expandFromCenter(-amount);
        } else {
            throw new RuntimeException("can't narrow given area with given amount");
        }
    }

    public boolean contains(CellPosition position) {
        boolean result = true;

        // check left border
        if (position.row < origin.row)
            result = false;
        // check right border
        else if (position.row > origin.row + rowCount - 1)
            result = false;
        // check top border
        else if (position.column < origin.column)
            result = false;
        // check bottom border
        else if (position.column > origin.column + columnCount - 1)
            result = false;

        return result;
    }

    @Override
    public Iterator<CellPosition> iterator() {
        return new Iterator<CellPosition>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < rowCount * columnCount;
            }

            @Override
            public CellPosition next() {
                int row = origin.row + (currentIndex / columnCount);
                int column = origin.column + (currentIndex % columnCount);

                currentIndex++;

                return new CellPosition(row, column);
            }
        };
    }
}
