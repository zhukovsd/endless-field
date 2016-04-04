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

//        int count = 1000000;

//        ArrayList<CellPosition> positions = new ArrayList<>();
//        for (int row = 0; row < 3; row++) {
//            for (int column = 0; column < 3; column++) {
//                positions.add(new CellPosition(row, column));
//            }
//        }

        // 550ms w/o storing, 10000ms w/ sync storing, 1000ms w/ async storing with 5 threads
        // full reading - 3000ms
        long time = System.nanoTime();

        // 15k 50x50 chunks on 4GB RAM
        for (int chunkRow = 0; chunkRow < 200; chunkRow ++) {
            for (int chunkColumn = 0; chunkColumn < 200; chunkColumn ++) {
                List<CellPosition> positions = Arrays.asList(new CellPosition(chunkRow * 50, chunkColumn * 50));

                field.lockChunks(positions);
                try {

                } finally {
                    field.unlockChunks();
                }

                if (chunkColumn == 99)
                    System.out.println("chunk count = " + field.chunkMap.size());
            }
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

//        ArrayDeque
    }
}