package com.zhukovsd.experiments.concurrency.concurrentchunkaccess.threads;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkIdGenerator;
import com.zhukovsd.simplefield.SimpleField;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

/**
 * Created by ZhukovSD on 27.03.2016.
 */
public abstract class LockTestTask implements Runnable {
    protected SimpleField field;
    //    Iterable<CellPosition> positions;
    int cellRange;
    int maxCellPosition;

    Random rand = new Random();

    public LockTestTask(SimpleField field, int cellRange, int maxCellPosition) {
        this.field = field;
        this.cellRange = cellRange;
        this.maxCellPosition = maxCellPosition;
    }

    protected ArrayList<CellPosition> getCellPositions() {
        int originRow = rand.nextInt(maxCellPosition - cellRange + 1);
        int originColumn = rand.nextInt(maxCellPosition - cellRange + 1);

        ArrayList<CellPosition> positions = new ArrayList<>(cellRange * cellRange);
        for (int row = originRow; row < originRow + cellRange; row++) {
            for (int column = originColumn; column < originColumn + cellRange; column++) {
                positions.add(new CellPosition(row, column));
            }
        }
        return positions;
    }

    protected TreeSet<Integer> getChunkIdsToLock(Iterable<CellPosition> positions) {
        TreeSet<Integer> chunkIds = new TreeSet<>();
        for (CellPosition position : positions) chunkIds.add(ChunkIdGenerator.generateID(field.chunkSize, position));

        for (Integer id : chunkIds) field.provideAndLockChunk(id);

        return chunkIds;
    }
}
