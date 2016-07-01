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
import com.zhukovsd.endlessfield.field.EndlessFieldActionInvoker;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunkFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public class ChunkIdGenerator {
    public static int idFactor = 40000;

    public static int chunkIdByChunkRowAndColumn(int chunkRow, int chunkColumn) {
        return chunkRow * idFactor + chunkColumn;
    }

    public static int chunkIdByPosition(ChunkSize chunkSize, CellPosition position) {
//        return 0;
//        return (position.row / chunkSize.rowCount) * idFactor + (position.column / chunkSize.columnCount);
        return chunkIdByChunkRowAndColumn(position.row / chunkSize.rowCount, position.column / chunkSize.columnCount);
    }

    public static CellPosition chunkOrigin(ChunkSize chunkSize, int chunkId) {
        return new CellPosition(
                (chunkId / idFactor) * chunkSize.rowCount,
                (chunkId % idFactor) * chunkSize.columnCount
        );
    }

    public static int chunkRow(Integer chunkId) {
        return chunkId / idFactor;
    }

    public static int chunkColumn(Integer chunkId) {
        return chunkId % idFactor;
    }

    public static Collection<Integer> chunkIdsByArea(ChunkSize chunkSize, EndlessFieldArea area) {
        // TODO: 29.06.2016 area (0, 9, 6, 6) with chunk size (5, 5) gives incorrect result (fix in js too!)

        ArrayList<Integer> chunkIds = new ArrayList<>();

        Integer originChunkId = ChunkIdGenerator.chunkIdByPosition(chunkSize, new CellPosition(area.origin.row, area.origin.column));

        CellPosition rightBottomAreaPosition = new CellPosition(
                area.origin.row + area.rowCount - 1, area.origin.column + area.columnCount - 1
        );
        Integer rightBottomChunkId = chunkIdByPosition(chunkSize, rightBottomAreaPosition);

        int vChunkCount = chunkRow(rightBottomChunkId) - chunkRow(originChunkId) + 1;
        int hChunkCount = chunkColumn(rightBottomChunkId) - chunkColumn(originChunkId) + 1;

        for (int chunkRow = 0; chunkRow < vChunkCount; chunkRow++) {
            for (int chunkColumn = 0; chunkColumn < hChunkCount; chunkColumn++) {
                chunkIds.add(originChunkId + chunkRow * ChunkIdGenerator.idFactor + chunkColumn);
            }
        }

        return chunkIds;
    }

    // TODO: 28.06.2016 move to EndlessFieldArea class
    public static EndlessFieldArea chunkAreaById(EndlessField<?> field, int chunkId) {
        ChunkSize chunkSize = field.chunkSize;
        return new EndlessFieldArea(
                field, ChunkIdGenerator.chunkOrigin(chunkSize, chunkId), chunkSize.rowCount, chunkSize.columnCount
        );
    }
}
