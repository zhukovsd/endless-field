package com.zhukovsd;

import com.zhukovsd.endlessfield.field.*;
import com.zhukovsd.simplefield.*;

import java.util.*;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SimpleField field = new SimpleField(new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());
        Random rand = new Random();

        int count = 1000000;

        ArrayList<CellPosition> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(new CellPosition(rand.nextInt(1000), rand.nextInt(1000)));
        }

        // 550ms w/o storing, 10000ms w/ sync storing, 1000ms w/ async storing with 5 threads
        // full reading - 3000ms
        long time = System.nanoTime();

        field.lockChunks(positions);
        try {
//            for (CellPosition position : positions)
//                field.getCell(position);
            field.getCells(positions);
        } finally {
            field.unlockChunks();
        }
//        field.getCells(positions);
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