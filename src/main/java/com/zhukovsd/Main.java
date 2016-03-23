package com.zhukovsd;

import com.zhukovsd.endlessfield.field.*;
import com.zhukovsd.simplefield.*;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SimpleField field = new SimpleField(new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());
        Random rand = new Random();

        // 500ms w/o storing, 10000ms w/ sync storing, 1000ms w/ async storing with 5 threads
        // full reading - 3000ms
        long time = System.nanoTime();
        for (int i = 0; i < 999999; i++) {
            field.getEntry(new CellPosition(rand.nextInt(1000), rand.nextInt(1000)));
        }
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
    static class UpdateRunnable implements Runnable {
        Iterable<SimpleFieldCell> cells;

        public UpdateRunnable(Iterable<SimpleFieldCell> cells) {
            this.cells = cells;
        }

        @Override
        public void run() {
            while (true) {
//                for (SimpleFieldCell cell : cells) {
//                    cell.setChecked(!cell.isChecked());
//                }

                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();

        SimpleField field = new SimpleField(new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());

        int maxRow = 3, maxColumn = 3;
        ArrayList<SimpleFieldCell> cells = new ArrayList<>();

        for (int row = 0; row < maxRow; row++) {
            for (int column = 0; column < maxColumn; column++) {
                cells.add(field.getCell(new CellPosition(row, column)));
            }
        }

//        for (int i = 0; i < 1; i++) {
//            exec.submit(new UpdateRunnable(new ArrayList<SimpleFieldCell>(cells)));
//        }

        while (true) {
            Set<Boolean> statesSet = new HashSet<>();

            for (int row = 0; row < maxRow; row++) {
                for (int column = 0; column < maxColumn; column++) {
                    statesSet.add(field.getCell(new CellPosition(row, column)).isChecked());
                }
            }

            if (statesSet.size() == 1) {
                System.out.println("ok");
            } else {
                System.out.println("sync issue");
            }

            TimeUnit.SECONDS.sleep(1);
        }
    }
}