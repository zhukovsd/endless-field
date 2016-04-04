package com.zhukovsd.endlessfield.fielddatasource;

import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 22.03.2016.
 */
public class StoreChunkTask<T extends EndlessFieldCell> implements Runnable {
    // TODO: 03.04.2016 remove debug field
    public static AtomicInteger storeCount = new AtomicInteger(0);

    EndlessFieldDataSource<T> dataSource;
    EndlessFieldChunk<T> chunk;
    int chunkId;

    public StoreChunkTask(EndlessFieldDataSource<T> dataSource, EndlessFieldChunk<T> chunk, int chunkId) {
        this.dataSource = dataSource;
        this.chunk = chunk;
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
        dataSource.storeChunk(chunk, chunkId);

        // we assume that chunk is guaranteed to exist
        chunk.setStored(true);

        storeCount.incrementAndGet();
    }
}
