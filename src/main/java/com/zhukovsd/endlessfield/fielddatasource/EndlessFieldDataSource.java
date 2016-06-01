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

package com.zhukovsd.endlessfield.fielddatasource;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;

import java.util.Map;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public interface EndlessFieldDataSource<T extends EndlessFieldCell> {
    boolean containsChunk(Integer chunkId);
    EndlessFieldChunk<T> getChunk(Integer chunkId, ChunkSize chunkSize);

    void storeChunk(EntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap, int chunkId, EndlessFieldChunk<T> chunk) throws InterruptedException;
    void modifyEntries(Map<CellPosition, T> entries);
}
