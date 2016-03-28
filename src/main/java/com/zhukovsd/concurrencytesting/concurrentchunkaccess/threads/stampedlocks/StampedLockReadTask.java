package com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.stampedlocks;

import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class StampedLockReadTask extends LockTestTask {
    static class StampedLockData {
        StampedLock lock;
        long stamp = 0;

        public StampedLockData(StampedLock lock) {
            this.lock = lock;
        }
    }

    public static AtomicInteger counter = new AtomicInteger(0);
    public static AtomicInteger optimisticCounter = new AtomicInteger(0);

    public StampedLockReadTask(SimpleField field, int cellRange, int maxCellPosition) {
        super(field, cellRange, maxCellPosition);
    }

    @Override
    public void run() {
        try {
            StampedLock lock = field.chunkMap.get(0).stampedLock;
//            ArrayList<StampedLockData> locks = new ArrayList<>();

            while (true) {
                counter.incrementAndGet();

                ArrayList<CellPosition> positions = getCellPositions();
                Iterable<Integer> chunkIds = getChunkIdsToLock(positions);
//
//                locks.clear();
//                for (Integer chunkId : chunkIds)
//                    locks.add(new StampedLockData(field.chunkMap.get(chunkId).stampedLock));
//
//                for (StampedLockData lockData : locks) lockData.stamp = lockData.lock.tryOptimisticRead();
//                // optimistic reading
//                Iterable<SimpleFieldCell> cells = field.getCells(positions);
//
//                boolean isValid = true;
//
//                for (StampedLockData lockData : locks) {
//                    if (!lockData.lock.validate(lockData.stamp)) {
//                        isValid = false;
//                        break;
//                    }
//                }
//
//                if (!isValid) {
//                    for (StampedLockData lockData : locks) lockData.stamp = lockData.lock.readLock();
//                    try {
//                        // reading
//                        cells = field.getCells(positions);
//
//                        // check states
//                    } finally {
//                        for (StampedLockData lockData : locks) lockData.lock.unlockRead(lockData.stamp);
//                    }
//                }

                long stamp = lock.tryOptimisticRead();
                // optimistic reading
                Iterable<SimpleFieldCell> cells = field.getCells(positions);

                if (!lock.validate(stamp)) {
                    stamp = lock.readLock();
                    try {
                        // reading
                        cells = field.getCells(positions);

                        // check is states are consistent
                        HashSet<Boolean> states = new HashSet<>();
                        for (SimpleFieldCell cell : cells) states.add(cell.isChecked());
                        if (states.size() > 1)
                            System.out.println("hi there");
                        boolean state = (states.contains(true));
                    } finally {
                        lock.unlockRead(stamp);
                    }
                } else {
                    optimisticCounter.incrementAndGet();
                }
            }
        } catch (Exception e) {
//          System.out.println(e.getMessage());
        }
    }
}
