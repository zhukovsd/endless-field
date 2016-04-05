package com.zhukovsd.endlessfield.fielddatasource;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 29.03.2016.
 */
public class UpdateCellTask<T extends EndlessFieldCell> implements Runnable {
    // TODO: 03.04.2016 remove debug field
    public static AtomicInteger updateCount = new AtomicInteger(0);

    EndlessField<T> field;
    EndlessFieldDataSource<T> dataSource;
    Map<CellPosition, T> entries;
    Set<Integer> chunkIds;

    public UpdateCellTask(EndlessField field, EndlessFieldDataSource<T> dataSource, Map<CellPosition, T> entries,
                          Set<Integer> chunkIds) {
        this.field = field;
        this.dataSource = dataSource;
        this.entries = entries;
        this.chunkIds = chunkIds;
    }

    @Override
    public void run() {
        // TODO: 23.03.2016 handle store exceptions / errors
        dataSource.modifyEntries(entries);

        // commit stored state
        for (Integer chunkId : chunkIds) {
            // we assume that chunk is guaranteed to exists, and we can get it directly from chunkMap
            field.chunkMap.get(chunkId).updateTaskCount.decrementAndGet();
        }

        updateCount.incrementAndGet();


    }
}
