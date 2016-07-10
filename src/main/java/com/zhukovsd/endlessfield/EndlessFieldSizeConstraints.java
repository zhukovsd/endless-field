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
 * Created by ZhukovSD on 28.06.2016.
 */
public class EndlessFieldSizeConstraints {
    public final int chunkRowCount, chunkColumnCount;

    public EndlessFieldSizeConstraints(int chunkRowCount, int chunkColumnCount) {
        this.chunkRowCount = chunkRowCount;
        this.chunkColumnCount = chunkColumnCount;
    }

    public int maxRow(ChunkSize chunkSize) {
        return this.chunkRowCount * chunkSize.rowCount - 1;
    }

    public int maxColumn(ChunkSize chunkSize) {
        return this.chunkColumnCount * chunkSize.columnCount - 1;
    }
}
