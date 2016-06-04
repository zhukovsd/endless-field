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
public class ChunkIdGenerator {
    public static int idFactor = 40000;

    public static int generateID(ChunkSize chunkSize, CellPosition position) {
//        return 0;
        return (position.row / chunkSize.rowCount) * idFactor + (position.column / chunkSize.columnCount);
    }

    public static CellPosition chunkOrigin(ChunkSize chunkSize, int chunkId) {
        return new CellPosition(
                (chunkId / idFactor) * chunkSize.rowCount,
                (chunkId % idFactor) * chunkSize.columnCount
        );
    }
}