package com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.reentrantlocks;

import com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads.LockTestTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 26.03.2016.
 */
public class ReentrantLockReadTask extends LockTestTask {
    public static AtomicInteger counter = new AtomicInteger(0);

    public ReentrantLockReadTask(SimpleField field, int cellRange, int maxCellPosition) {
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

                    // read
                    Iterable<SimpleFieldCell> cells = field.getCells(positions);

//                    HashSet<Boolean> states = new HashSet<>();
//                    for (SimpleFieldCell cell : cells) states.add(cell.isChecked());
//                    if (states.size() > 1)
//                        System.out.println("hi there");
//                    boolean state = (states.contains(true));
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
