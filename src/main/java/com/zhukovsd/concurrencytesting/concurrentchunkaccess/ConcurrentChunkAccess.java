package com.zhukovsd.concurrencytesting.concurrentchunkaccess;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.zhukovsd.concurrencytesting.concurrentchunkaccess.WriteTask.counter;

class WriteTask implements Runnable {
    static AtomicInteger counter = new AtomicInteger();

    SimpleField field;
    Iterable<CellPosition> positions;

    WriteTask(SimpleField field, Iterable<CellPosition> positions) {
        this.field = field;
        this.positions = positions;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ArrayList<SimpleFieldCell> cells = new ArrayList<>();

                field.lockChunks(positions);
                try {
                    counter.incrementAndGet();

                    // reading
                    for (CellPosition position : positions)
                        cells.add(field.getCell(position));

                    // modifying
                    for (SimpleFieldCell cell : cells) {
                        cell.setChecked(!cell.isChecked());
                    }
                } finally {
                    field.unlockChunks();
                }
            }
        } catch (Exception e) {
            //
        }
    }
}

/**
 * Created by ZhukovSD on 24.03.2016.
 */
public class ConcurrentChunkAccess {
    public static void main(String[] args) throws InterruptedException {
        // Run N threads modifies positions in chunk with same ID simultaneously.
        // One thread checks if positions state are correct.
        // If no locking provided on chunk, data may be overwritten during reading, or vice versa
        // Locking on chunk level ensures that read/write of another chunks are not blocked

        ExecutorService exec = Executors.newCachedThreadPool();

        SimpleField field = new SimpleField(
                new ChunkSize(50, 50),
                new EndlessFieldDataSource<SimpleFieldCell>() {
                    @Override
                    public boolean containsChunk(Integer chunkId) {
                        return false;
                    }

                    @Override
                    public EndlessFieldChunk<SimpleFieldCell> getChunk(Integer chunkId, ChunkSize chunkSize) {
                        return null;
                    }

                    @Override
                    public void storeChunk(EndlessFieldChunk<SimpleFieldCell> chunk, int chunkId) {
                        //
                    }
                },
                new SimpleFieldCellFactory()
        );

        int maxRow = 3, maxColumn = 3;
        ArrayList<CellPosition> positions = new ArrayList<>();

        for (int row = 0; row < maxRow; row++) {
            for (int column = 0; column < maxColumn; column++) {
                positions.add(new CellPosition(row, column));
            }
        }

        int count = 20;

        // writers
        for (int i = 0; i < count; i++) {
            exec.execute(new WriteTask(field, positions));
        }

        // reader
        exec.execute(
                () -> {
                    try {
                        while (true) {
                            Set<Boolean> statesSet = new HashSet<>();

                            field.lockChunks(positions);
                            try {
                                for (int row = 0; row < maxRow; row++) {
                                    for (int column = 0; column < maxColumn; column++) {
                                        statesSet.add(field.getCell(new CellPosition(row, column)).isChecked());
                                    }
                                }
                            } finally {
                                field.unlockChunks();
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
        );

        TimeUnit.SECONDS.sleep(10);
        exec.shutdownNow();

        System.out.println(counter);
    }
}
