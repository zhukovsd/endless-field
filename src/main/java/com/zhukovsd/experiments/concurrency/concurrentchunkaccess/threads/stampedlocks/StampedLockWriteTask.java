package com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.stampedlocks;

import com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class StampedLockWriteTask extends LockTestTask {
    public static AtomicInteger counter = new AtomicInteger();
    public static AtomicInteger optimisticCounter = new AtomicInteger();

    public StampedLockWriteTask(SimpleField field, int cellRange, int maxCellPosition) {
        super(field, cellRange, maxCellPosition);
    }

    @Override
    public void run() {
        StampedLock lock = null; // field.chunkMap.get(0).stampedLock;


        try {
            while (true) {
                boolean readLocked = false;

                ArrayList<CellPosition> positions = getCellPositions();
                Iterable<Integer> chunkIds = getChunkIdsToLock(positions);

//                long stamp = lock.tryOptimisticRead();
//                long optimisticStamp = stamp;
//                // optimistic reading
//                Iterable<SimpleFieldCell> cells = field.getCells(positions);
//
//                //region check is states are consistent
//                HashSet<Boolean> states = new HashSet<>();
//                for (SimpleFieldCell cell : cells) states.add(cell.isChecked());
////                if (states.size() > 1)
////                    System.out.println("hi there");
//                boolean state = (states.contains(true));
//                //endregion
//
//                if (!lock.validate(stamp)) {
//                    log.add("optimistic read failed, locking");
//
//                    stamp = lock.readLock();
//                    try {
//                        readLocked = true;
//
//                        // reading
//                        cells = field.getCells(positions);
//
//                        //region check if states are consistent
//                        states = new HashSet<>();
//                        for (SimpleFieldCell cell : cells) states.add(cell.isChecked());
//                        if (states.size() > 1)
//                            System.out.println("hi there");
//                        state = (states.contains(true));
//                        //endregion
//                    } finally {
////                        lock.unlockRead(stamp);
//                    }
//                }
//
//                boolean writeDone = false;
//                try {
//                    while (!writeDone) {
//                        if (!lock.validate(optimisticStamp)) {
//                            cells = field.getCells(positions);
//
//                            //region check if states are consistent
//                            states = new HashSet<>();
//                            for (SimpleFieldCell cell : cells) states.add(cell.isChecked());
//                            if (states.size() > 1)
//                                System.out.println("hi there");
//                            state = (states.contains(true));
//                            //endregion
//                        } else {
//                            log.add("successful optimistic read");
//                            optimisticCounter.incrementAndGet();
//                        }
//
//                        long writeStamp = lock.tryConvertToWriteLock(stamp);
//
//                        if (writeStamp != 0L) {
//                            log.add("write lock successful, ready to modify data");
//
//                            stamp = writeStamp;
//
//                            counter.incrementAndGet();
//
//                            // region 123
//                            HashSet<Boolean> states2 = new HashSet<>();
//                            for (SimpleFieldCell cell : cells) states2.add(cell.isChecked());
//                            boolean state2 = (states2.contains(true));
//
//                            if (state != state2)
//                                System.out.println("data has been overwritten between read.unlock() and write.lock()");
//                            // endregion
//
//                            for (SimpleFieldCell cell : cells)
//                                cell.setChecked(!cell.isChecked());
//
//                            writeDone = true;
//                        } else {
//                            log.add("tryConvertToWriteLock from optimistic read failed");
////                            if (lock.isReadLocked()) {
//                            if (readLocked) {
//                                lock.unlockRead(stamp);
//                                log.add("uncling read before locking write");
//                            }
//
//                            log.add("write locking");
//                            stamp = lock.writeLock();
//                        }
//                    }
//                } finally {
////                    if (lock.isReadLocked())
////                        lock.unlockWrite(stamp);
////                    sout
//                    lock.unlock(stamp);
//                }

                long stamp = lock.readLock();
                try {
                    // reading
                    Iterable<SimpleFieldCell> cells = field.getCells(positions);

                    //region check is states are consistent
//                    HashSet<Boolean> states = new HashSet<>();
//                    for (SimpleFieldCell cell : cells) states.add(cell.isChecked());
//                    if (states.size() > 1)
//                        System.out.println("hi there");
//                    boolean state = (states.contains(true));
                    //endregion

                    boolean isRead = false;
                    while (!isRead) {
                        long writeStamp = lock.tryConvertToWriteLock(stamp);
                        if (writeStamp != 0L) {
                            stamp = writeStamp;

                            counter.incrementAndGet();

                            // region 123
//                            HashSet<Boolean> states2 = new HashSet<>();
//                            for (SimpleFieldCell cell : cells) states2.add(cell.isChecked());
//                            boolean state2 = (states2.contains(true));
//                            if (state != state2)
//                                System.out.println("data has been overwritten between read.unlock() and write.lock()");
                            // endregion

                            for (SimpleFieldCell cell : cells)
                                cell.setChecked(!cell.isChecked());

                            isRead = true;
                        } else {
                            lock.unlockRead(stamp);

                            // this is the place where data may be overwritten!

                            stamp = lock.writeLock();
                        }
                    }
                } finally {
                    lock.unlock(stamp);
                }
            }
        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            System.out.println(lock.toString());
//            e.printStackTrace();
        }
    }
}
