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

import java.util.Iterator;

/**
 * Created by ZhukovSD on 09.06.2016.
 */
public class EndlessFieldArea implements Iterable<CellPosition> {
    public CellPosition origin;
    public int rowCount, columnCount;

    public EndlessFieldArea(CellPosition origin, int rowCount, int columnCount) {
        this.origin = origin;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public EndlessFieldArea expandFromCenter(int amount) {
        int leftBoundary = Math.max(origin.column - amount, 0);
        int topBoundary = Math.max(origin.row - amount, 0);

        // TODO: 09.06.2016 field size constraints
        int rightBoundary = (origin.column + columnCount - 1) + amount;
        int bottomBoundary = (origin.row + rowCount - 1) + amount;

        origin = new CellPosition(topBoundary, leftBoundary);

        rowCount = bottomBoundary - topBoundary + 1;
        columnCount = rightBoundary - leftBoundary + 1;

        return this;
    };

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
