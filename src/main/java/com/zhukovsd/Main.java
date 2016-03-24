package com.zhukovsd;

import com.zhukovsd.endlessfield.field.*;
import com.zhukovsd.simplefield.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SimpleField field = new SimpleField(new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());
        Random rand = new Random();

        // 550ms w/o storing, 10000ms w/ sync storing, 1000ms w/ async storing with 5 threads
        // full reading - 3000ms
        long time = System.nanoTime();
        ArrayList<CellPosition> positions = new ArrayList<>(1000000);
        for (int i = 0; i < 1000000; i++) {
//            field.getEntry(new CellPosition(rand.nextInt(1000), rand.nextInt(1000)));
            positions.add(new CellPosition(rand.nextInt(1000), rand.nextInt(1000)));
        }
        field.getCells(positions);
        time = (System.nanoTime() - time) / 1000000;
        System.out.println(time + "ms");

//        field.getCell(new CellPosition(0, 0)).setChecked(true);

//        int chunkCount = 400;
//        while (true) {
//            int storedCount = 0;
//            for (Map.Entry<Integer, EndlessFieldChunk<SimpleFieldCell>> chunkEntry : field.chunkSet()) {
//                if (chunkEntry.getValue().isStored())
//                    storedCount++;
//            }
//
//            System.out.println("stored = " + storedCount + "/" + chunkCount);
//
//            TimeUnit.SECONDS.sleep(1);
//        }
    }
}

class UpdateSynchronization {
    static abstract class CancelableTask implements Runnable {
        static volatile boolean cancelled = false;
        static void cancel() {
            cancelled = true;
        }
    }

//    static class UpdateTask extends CancelableTask {
//        SimpleField field;
//        Iterable<SimpleFieldCell> cells;
//
//        UpdateTask(SimpleField field, Iterable<SimpleFieldCell> cells) {
//            this.field = field;
//            this.cells = cells;
//        }
//
//        @Override
//        public void run() {
//            while (!cancelled) {
////                field.test(0).lock.lock();
//                field.lockChunk(0);
//
//                try {
//                    for (SimpleFieldCell cell : cells) {
//                        cell.setChecked(!cell.isChecked());
//                    }
//                } finally {
//                    field.unlockChunk();
//                }
//            }
//        }
//    }

    static class EntryLockCounterTask extends CancelableTask {
        SimpleField field;

        static long counter = 0;

        public EntryLockCounterTask(SimpleField field) {
            this.field = field;
        }

        @Override
        public void run() {
            while (!cancelled) {
                field.lockChunk(0);
                try {
                    counter++;
                } finally {
                    field.unlockChunk();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService exec = Executors.newCachedThreadPool();

        SimpleField field = new SimpleField(new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());

        int maxRow = 3, maxColumn = 3;
        ArrayList<SimpleFieldCell> cells = new ArrayList<>();

        for (int row = 0; row < maxRow; row++) {
            for (int column = 0; column < maxColumn; column++) {
                cells.add(field.getCell(new CellPosition(row, column)));
            }
        }

//        for (int i = 0; i < 9; i++) {
//            exec.submit(new UpdateTask(field, new ArrayList<SimpleFieldCell>(cells)));
//        }
        exec.submit(new EntryLockCounterTask(field));

//        while (true) {
//            Set<Boolean> statesSet = new HashSet<>();
//
//            field.test(0).lock.lock();
//            try {
//                for (int row = 0; row < maxRow; row++) {
//                    for (int column = 0; column < maxColumn; column++) {
//                        statesSet.add(field.getCell(new CellPosition(row, column)).isChecked());
//                    }
//                }
//            } finally {
//                field.test(0).lock.unlock();
//            }
//
//            if (statesSet.size() == 1) {
//                System.out.println("ok");
//            } else {
//                System.out.println("sync issue");
//            }
//
//            TimeUnit.SECONDS.sleep(1);
//        }

//        TimeUnit.SECONDS.sleep(10); // 1395k, 1400k
//
//        CancelableTask.cancel();
//        exec.shutdown();
//
//        if (!exec.awaitTermination(250, TimeUnit.MILLISECONDS))
//            System.out.println("not terminated");
//
//        System.out.println("entry count = " + EntryLockCounterTask.counter);
//
//        exec.shutdown();
    }
}