package com.zhukovsd.endlessfield.fielddatasource;

import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.enrtylockingconcurrenthashmap.StripedEntryLockingConcurrentHashMap;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

/**
 * Created by ZhukovSD on 22.03.2016.
 */
public class StoreChunkTask<T extends EndlessFieldCell> implements Runnable {
    // TODO: 03.04.2016 remove debug field
    public static AtomicInteger storeCount = new AtomicInteger(0);

    private final EndlessFieldDataSource<T> dataSource;
    private final StripedEntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap;
    private final int chunkId;

    public StoreChunkTask(EndlessFieldDataSource<T> dataSource,
                          StripedEntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap, int chunkId)
    {
        this.dataSource = dataSource;
        this.chunkMap = chunkMap;
        this.chunkId = chunkId;
    }

    @Override
    public void run() {
//        try {
//            TimeUnit.MILLISECONDS.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // TODO: 23.03.2016 handle store exceptions / errors
        try {
            dataSource.storeChunk(chunkMap, chunkId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // we assume that chunk is guaranteed to exists, and we can get it directly from chunkMap
        // TODO: 25.04.2016 log null pointer exception
        chunkMap.getValueNonLocked(chunkId).setStored(true);

        storeCount.incrementAndGet();
    }
}
