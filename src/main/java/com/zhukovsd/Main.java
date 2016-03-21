package com.zhukovsd;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;
import com.zhukovsd.simplefield.SimpleFieldDataSource;

import java.util.Random;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public class Main {
    public static void main(String[] args) {
        SimpleField field = new SimpleField(new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());
        Random rand = new Random();

        // 2s w/o storing, 21s w/ sync storing
        // full reading - 4.3s
        long time = System.nanoTime();
        for (int i = 0; i < 999999; i++) {
            field.getCell(new CellPosition(rand.nextInt(1000), rand.nextInt(1000)));
        }
        time = (System.nanoTime() - time) / 1000000;

        System.out.println(time + "ms");
    }
}
