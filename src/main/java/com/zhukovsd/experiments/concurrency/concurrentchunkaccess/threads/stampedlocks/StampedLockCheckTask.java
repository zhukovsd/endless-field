package com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.stampedlocks;

import com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.simplefield.SimpleField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class StampedLockCheckTask extends LockTestTask {
    static class StampedLockData {
        StampedLock lock;
        long stamp = 0;

        public StampedLockData(StampedLock lock) {
            this.lock = lock;
        }
    }

    public StampedLockCheckTask(SimpleField field, int cellRange, int maxCellPosition) {
        super(field, cellRange, maxCellPosition);
    }

    @Override
    public void run() {
        try {
            ArrayList<StampedLockData> locks = new ArrayList<>();

            while (true) {
                Set<Boolean> statesSet = new HashSet<>();

                ArrayList<CellPosition> positions = getCellPositions();
                Iterable<Integer> chunkIds = getChunkIdsToLock(positions);

                locks.clear();
                for (Integer chunkId : chunkIds)
                    locks.add(new StampedLockData(field.chunkMap.get(chunkId).stampedLock));

//                long stamp = lock.readLock();
                for (StampedLockData lockData : locks) lockData.stamp = lockData.lock.readLock();
                try {
                    for (CellPosition position : positions)
                        statesSet.add(field.getCell(position).isChecked());
                } finally {
//                    lock.unlockRead(stamp);
                    for (StampedLockData lockData : locks) lockData.lock.unlock(lockData.stamp);
                }

                if (statesSet.size() == 1) {
                    System.out.println("ok " + statesSet.toString());
                } else {
                    System.out.println("sync issue");
                }

                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            System.out.println("checked exit");
        }
    }
}
