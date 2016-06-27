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

package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkIdGenerator;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.field.EndlessFieldChunkFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by ZhukovSD on 25.06.2016.
 */
public class SimpleFieldChunkFactory extends EndlessFieldChunkFactory<SimpleFieldCell> {
    SimpleFieldChunkFactory(SimpleField field) {
        super(field);
    }

    @Override
    protected EndlessFieldChunk<SimpleFieldCell> generateChunk(Integer chunkId, Collection<Integer> lockedChunkIds) {
        EndlessFieldChunk<SimpleFieldCell> chunk = super.generateChunk(chunkId, lockedChunkIds);
        CellPosition chunkOrigin = ChunkIdGenerator.chunkOrigin(field.chunkSize, chunkId);

        for (int row = 0; row < field.chunkSize.rowCount; row++) {
            for (int column = 0; column < field.chunkSize.columnCount; column++) {
                SimpleFieldCell cell = new SimpleFieldCell(false);
//                if ((chunkId == 5) && (row < 4) && (column < 4))
//                    cell.setChecked(true);

                chunk.put(new CellPosition(chunkOrigin.row + row, chunkOrigin.column + column), cell);
            }
        }

//        if (lockedChunkIds.contains(5)) {
//            System.out.println("related chunk exists");
//
//            Map<CellPosition, SimpleFieldCell> copyEntries = field.getEntriesByChunkIds(Collections.singleton(5));
//            CellPosition copyOrigin = ChunkIdGenerator.chunkOrigin(field.chunkSize, 5);
//
//            for (int row = 0; row < field.chunkSize.rowCount; row++) {
//                for (int column = 0; column < field.chunkSize.columnCount; column++) {
//                    chunk.get(new CellPosition(chunkOrigin.row + row, chunkOrigin.column + column)).setChecked(
//                            copyEntries.get(new CellPosition(copyOrigin.row + row, copyOrigin.column + column)).isChecked()
//                    );
//                }
//            }
//        } else {
//            System.out.println("related chunk not exists");
//        }

        return chunk;
    }
}
