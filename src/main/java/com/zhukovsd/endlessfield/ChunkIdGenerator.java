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

import java.util.ArrayList;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public class ChunkIdGenerator {
    public static int idFactor = 40000;

    public static int chunkIdByChunkRowAndColumn(int chunkRow, int chunkColumn) {
        return chunkRow * idFactor + chunkColumn;
    }

    public static int generateID(ChunkSize chunkSize, CellPosition position) {
        return chunkIdByChunkRowAndColumn(position.row / chunkSize.rowCount, position.column / chunkSize.columnCount);
    }

    public static CellPosition chunkOrigin(ChunkSize chunkSize, int chunkId) {
        return new CellPosition(
                (chunkId / idFactor) * chunkSize.rowCount,
                (chunkId % idFactor) * chunkSize.columnCount
        );
    }

    public static Iterable<Integer> chunkIdsByArea(ChunkSize chunkSize, EndlessFieldArea area) {
        ArrayList<Integer> chunkIds = new ArrayList<>();

        Integer originChunkId = ChunkIdGenerator.generateID(chunkSize, new CellPosition(area.origin.row, area.origin.column));

        int vChunkCount = (area.origin.row / chunkSize.rowCount) * chunkSize.rowCount;
        vChunkCount = area.origin.row + area.rowCount - vChunkCount;
        vChunkCount = vChunkCount / chunkSize.rowCount;
        if (!((area.origin.row % chunkSize.rowCount == 0) && (area.rowCount % chunkSize.rowCount == 0)))
            vChunkCount++;

        int hChunkCount = (area.origin.column / chunkSize.columnCount) * chunkSize.columnCount;
        hChunkCount = area.origin.column + area.columnCount - hChunkCount;
        hChunkCount = hChunkCount / chunkSize.columnCount;
        if (!((area.origin.column % chunkSize.columnCount == 0) && (area.columnCount % chunkSize.columnCount == 0)))
            hChunkCount++;

        for (int chunkRow = 0; chunkRow < vChunkCount; chunkRow++) {
            for (int chunkColumn = 0; chunkColumn < hChunkCount; chunkColumn++) {
                chunkIds.add(originChunkId + chunkRow * ChunkIdGenerator.idFactor + chunkColumn);
            }
        }

        return chunkIds;
    }
}
