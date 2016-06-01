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

import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 22.03.2016.
 */
public class StoreChunkTask<T extends EndlessFieldCell> implements Runnable {
    // TODO: 03.04.2016 remove debug field
    public static AtomicInteger storeCount = new AtomicInteger(0);

    private final EndlessFieldDataSource<T> dataSource;
    private final EntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap;
    private final int chunkId;
    private final EndlessFieldChunk<T> chunk;

    public StoreChunkTask(EndlessFieldDataSource<T> dataSource,
        EntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap, int chunkId, EndlessFieldChunk<T> chunk)
    {
        this.dataSource = dataSource;
        this.chunkMap = chunkMap;
        this.chunkId = chunkId;
        this.chunk = chunk;
    }

    @Override
    public void run() {
//        try {
//            TimeUnit.MILLISECONDS.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        try {
            // TODO: 23.03.2016 handle store exceptions / errors
            try {
                dataSource.storeChunk(chunkMap, chunkId, chunk);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // we assume that chunk is guaranteed to exists, and we can get it directly from chunkMap
            // TODO: 25.04.2016 log null pointer exception
            chunk.setStored(true);

            storeCount.incrementAndGet();

//            if (storeCount.get() % 100 == 0)
//                System.out.println(storeCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
