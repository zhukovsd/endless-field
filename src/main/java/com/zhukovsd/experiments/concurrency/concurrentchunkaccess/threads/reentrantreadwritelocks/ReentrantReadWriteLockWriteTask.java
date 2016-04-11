package com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.reentrantreadwritelocks;

import com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class ReentrantReadWriteLockWriteTask extends LockTestTask {
    public static AtomicInteger counter = new AtomicInteger();

    public ReentrantReadWriteLockWriteTask(SimpleField field, int cellRange, int maxCellPosition) {
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

//                    HashSet<Boolean> states = new HashSet<>();
//                    for (SimpleFieldCell cell : cells) states.add(cell.isChecked());
//                    if (states.size() > 1)
//                        System.out.println("hi there");
//                    boolean state = (states.contains(true));

//                    rwl.readLock().unlock();
                    for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.readLock().unlock();

                    // ! some thread may acquire write lock before we did, in this case read data may already be overwritten

//                    rwl.writeLock().lock();
                    for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.writeLock().lock();
                    try {
//                        HashSet<Boolean> states2 = new HashSet<>();
//                        for (SimpleFieldCell cell : cells) states2.add(cell.isChecked());
//                        boolean state2 = (states2.contains(true));
//
//                        if (state != state2)
//                            System.out.println("data has been overwritten between read.unlock() and write.lock()");

                        // modifying
                        for (SimpleFieldCell cell : cells) {
                            cell.setChecked(!cell.isChecked());
                        }

//                        rwl.readLock().lock();
                        for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.readLock().lock();
                    } finally {
//                        rwl.writeLock().unlock();
                        for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.writeLock().unlock();
                    }
                } finally {
//                    rwl.readLock().unlock();
                    for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.readLock().unlock();
                }
            }
        } catch (Exception e) {
//            System.out.println(e.getMessage());
        }
    }
}
