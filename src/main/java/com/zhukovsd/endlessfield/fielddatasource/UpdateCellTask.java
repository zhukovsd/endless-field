package com.zhukovsd.endlessfield.fielddatasource;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 29.03.2016.
 */
public class UpdateCellTask<T extends EndlessFieldCell> implements Runnable {
    // TODO: 03.04.2016 remove debug field
    public static AtomicInteger updateCount = new AtomicInteger(0);

    EndlessFieldDataSource<T> dataSource;
    Map<CellPosition, T> entries;

    public UpdateCellTask(EndlessFieldDataSource<T> dataSource, Map<CellPosition, T> entries) {
        this.dataSource = dataSource;
        this.entries = entries;
    }

    @Override
    public void run() {
        // TODO: 23.03.2016 handle store exceptions / errors
        dataSource.modifyEntries(entries);
        // commit stored state

        updateCount.incrementAndGet();


    }
}
