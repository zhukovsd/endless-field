package com.zhukovsd.experiments.concurrency.concurrentchunkproviding;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.EndlessFieldSizeConstraints;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ZhukovSD on 24.03.2016.
 */
public class ConcurrentChunkProviding {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Run N threads getting chunk with same ID simultaneously
        // if no locking provided on chunk generating/reading from data source, every thread will
        // generate/read its own chunk which will cause chunks duplication.
        // Correct behavior is for all threads to return the same chunk

        ExecutorService exec = Executors.newCachedThreadPool();

        SimpleField field = new SimpleField(
                16, new ChunkSize(50, 50),
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
                },
                new SimpleFieldCellFactory()
        );

        CompletionService<SimpleFieldCell> completionService = new ExecutorCompletionService<>(exec);

        int count = 5;

        ArrayList<Future<SimpleFieldCell>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            futures.add(completionService.submit(() -> {
                field.lockChunksByIds(Collections.singletonList(0));
                try {
                    return field.getCell(new CellPosition(0, 0));
                } finally {
                    field.unlockChunks();
                }
            }));
        }

        HashSet<SimpleFieldCell> cellSet = new HashSet<>();

        int received = 0;
        while (received < count) {
            Future<SimpleFieldCell> resultFuture = completionService.take(); //blocks if none available
            cellSet.add(resultFuture.get());
            received ++;
        }

        for (Future<SimpleFieldCell> future : futures) System.out.println(future.get());

        if (cellSet.size() == 1)
            System.out.println("correct behavior");
        else
            System.out.println("incorrect behavior");

        exec.shutdownNow();
    }
}
