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
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 29.03.2016.
 */
public class UpdateCellTask<T extends EndlessFieldCell> implements Runnable {
    // TODO: 03.04.2016 remove debug field
    public static AtomicInteger updateCount = new AtomicInteger(0);

    private final EndlessFieldDataSource<T> dataSource;
    private final EntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap;
    private final Map<CellPosition, T> entries;
    private final Set<Integer> chunkIds;

    public UpdateCellTask(EndlessFieldDataSource<T> dataSource,
                          EntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap,
                          Map<CellPosition, T> entries, Set<Integer> chunkIds)
    {
        this.dataSource = dataSource;
        this.chunkMap = chunkMap;
        this.entries = entries;
        this.chunkIds = chunkIds;
    }

    @Override
    public void run() {
//        if (Thread.currentThread().getName().contains("extended")) {
//            System.out.println("from extended thread pool");
//        }

        // TODO: 23.03.2016 handle store exceptions / errors
        dataSource.modifyEntries(entries);

        // commit stored state
        for (Integer chunkId : chunkIds) {
            // we assume that chunk is guaranteed to exists, and we can get it directly from chunkMap
            chunkMap.getValueNonLocked(chunkId).updateTaskCount.decrementAndGet();
        }

        updateCount.incrementAndGet();
    }
}
