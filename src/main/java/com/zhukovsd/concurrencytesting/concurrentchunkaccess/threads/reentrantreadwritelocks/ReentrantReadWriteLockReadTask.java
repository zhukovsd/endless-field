package com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantreadwritelocks;

import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class ReentrantReadWriteLockReadTask extends LockTestTask {
    public static AtomicInteger counter = new AtomicInteger(0);

    public ReentrantReadWriteLockReadTask(SimpleField field, int cellRange, int maxCellPosition) {
        super(field, cellRange, maxCellPosition);
    }

    @Override
    public void run() {
        try {
            while (true) {
                ArrayList<CellPosition> positions = getCellPositions();
                Iterable<Integer> chunkIds = getChunkIdsToLock(positions);

//                rwl.readLock().lock();
                for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.readLock().lock();
                try {
                    counter.incrementAndGet();

                    // reading
                    Iterable<SimpleFieldCell> cells = field.getCells(positions);
                } finally {
//                    rwl.readLock().unlock();
                    for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.readLock().unlock();
                }
            }
        } catch (Exception e) {
//          System.out.println(e.getMessage());
        }
    }
}
