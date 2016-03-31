package com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantlocks;

import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class ReentrantLockWriteTask extends LockTestTask {
    public static AtomicInteger counter = new AtomicInteger();

    public ReentrantLockWriteTask(SimpleField field, int cellRange, int maxCellPosition) {
        super(field, cellRange, maxCellPosition);
    }

    @Override
    public void run() {

        try {
            while (true) {
                ArrayList<CellPosition> positions = getCellPositions();
                Iterable<Integer> chunkIds = getChunkIdsToLock(positions);

//                field.chunkMap.get(0).lock.lock();
                for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).lock.lock();
                try {
                    counter.incrementAndGet();

                    // reading
                    Iterable<SimpleFieldCell> cells = field.getCells(positions);

                    // modifying
                    for (SimpleFieldCell cell : cells) {
                        cell.setChecked(!cell.isChecked());
                    }
                } finally {
//                    field.chunkMap.get(0).lock.unlock();
                    for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).lock.unlock();
                }
            }
        } catch (Exception e) {
            //
        }
    }
}