package com.zhukovsd.experiments.performance;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.EndlessFieldSizeConstraints;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 25.04.2016.
 */
public class ConcurrentChunkAccessExperiment {
    public static void main(String[] args) {
        ChunkSize chunkSize = new ChunkSize(50, 50);

        SimpleField field = new SimpleField(
                1000, chunkSize,
                new EndlessFieldSizeConstraints(40000, 40000),
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
                    public void storeChunk(EntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<SimpleFieldCell>> chunkMap, int chunkId, EndlessFieldChunk<SimpleFieldCell> chunk) throws InterruptedException {

                    }

                    @Override
                    public void modifyEntries(Map<CellPosition, SimpleFieldCell> entries) {

                    }
                }
        );

        ExecutorService exec = Executors.newCachedThreadPool();

        int chunkCount = 20, cellCount = 1, readersCount = 200;

        int maxRow = chunkCount * chunkSize.rowCount;
        int maxColumn = chunkCount * chunkSize.columnCount;

        AtomicInteger readCount = new AtomicInteger();

        for (int i = 0; i < readersCount; i++) {
            exec.submit((Runnable) () -> {
                Random rand = new Random();

                try {
                    while (true) {
                        List<CellPosition> positions = new ArrayList<>();
                        for (int j = 0; j < cellCount; j++) {
                            positions.add(new CellPosition(rand.nextInt(maxRow), rand.nextInt(maxColumn)));
                        }

                        field.lockChunksByPositions(positions);
                        try {
                            LinkedHashMap<CellPosition, SimpleFieldCell> entries = field.getEntries(positions);
                            for (Map.Entry<CellPosition, SimpleFieldCell> entry : entries.entrySet()) {
                                SimpleFieldCell cell = entry.getValue();

                                if (cell == null)
                                    System.out.println("cell can't be null");
                            }

                            readCount.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                field.unlockChunks();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        exec.submit((Runnable) () -> {
            int counter = 0;
            while (true) {
                try {
                    System.out.format("#%s chunk count = %s/%s, read count = %s\n",
                            counter, field.size(), chunkCount * chunkCount, readCount
                    );

                    counter++;
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
