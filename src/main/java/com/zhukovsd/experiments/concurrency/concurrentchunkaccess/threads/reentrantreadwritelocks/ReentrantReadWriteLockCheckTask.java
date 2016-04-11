package com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.reentrantreadwritelocks;

import com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.simplefield.SimpleField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class ReentrantReadWriteLockCheckTask extends LockTestTask {
    public ReentrantReadWriteLockCheckTask(SimpleField field, int cellRange, int maxCellPosition) {
        super(field, cellRange, maxCellPosition);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Set<Boolean> statesSet = new HashSet<>();

                ArrayList<CellPosition> positions = getCellPositions();
                Iterable<Integer> chunkIds = getChunkIdsToLock(positions);

//                rwl.readLock().lock();
                for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.readLock().lock();
                try {
                    for (CellPosition position : positions)
                        statesSet.add(field.getCell(position).isChecked());
                } finally {
//                    rwl.readLock().unlock();
                    for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).rwLock.readLock().unlock();
                }

                if (statesSet.size() == 1) {
                    System.out.println("ok");
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
