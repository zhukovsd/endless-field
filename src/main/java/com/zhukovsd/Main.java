package com.zhukovsd;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;
import com.zhukovsd.simplefield.SimpleFieldDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SimpleField field = new SimpleField(16, new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());
        Random rand = new Random();

        field.lockChunksByIds(Collections.singletonList(0));
        try {
            ArrayList<CellPosition> positions = new ArrayList<>();
            for (int row = 0; row < 5; row++) {
                for (int column = 0; column < 5; column++) {
                    positions.add(new CellPosition(row, column));
                }
            }

            LinkedHashMap<CellPosition, SimpleFieldCell> entries = field.getEntries(positions);

//            System.out.println(Gsonalizer.toJson(entries));
        } finally {
            field.unlockChunks();
        }
    }
}