package com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantlocks;

import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.simplefield.SimpleField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class ReentrantLockCheckTask extends LockTestTask {
    public ReentrantLockCheckTask(SimpleField field, int cellRange, int maxCellPosition) {
        super(field, cellRange, maxCellPosition);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Set<Boolean> statesSet = new HashSet<>();

                ArrayList<CellPosition> positions = getCellPositions();
                Iterable<Integer> chunkIds = getChunkIdsToLock(positions);

//                field.chunkMap.get(0).lock.lock();
                for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).lock.lock();
                try {
                    for (CellPosition position : positions)
                        statesSet.add(field.getCell(position).isChecked());
                } finally {
//                    field.chunkMap.get(0).lock.unlock();
                    for (Integer chunkId : chunkIds) field.chunkMap.get(chunkId).lock.unlock();
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
