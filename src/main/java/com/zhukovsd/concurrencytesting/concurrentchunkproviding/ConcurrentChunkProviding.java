package com.zhukovsd.concurrencytesting.concurrentchunkproviding;

import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;
import com.zhukovsd.simplefield.SimpleFieldDataSource;

import java.util.ArrayList;
import java.util.HashSet;
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

        SimpleField field = new SimpleField(new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());

        CompletionService<EndlessFieldChunk<SimpleFieldCell>> completionService = new ExecutorCompletionService<>(exec);

        int count = 5;

        ArrayList<Future<EndlessFieldChunk<SimpleFieldCell>>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            futures.add(completionService.submit(
                    () -> field.provideChunk(0)
            ));
        }

        HashSet<EndlessFieldChunk<SimpleFieldCell>> chunkSet = new HashSet<>();

        int received = 0;
        while (received < count) {
            Future<EndlessFieldChunk<SimpleFieldCell>> resultFuture = completionService.take(); //blocks if none available
            chunkSet.add(resultFuture.get());
            received ++;
        }

        for (Future<EndlessFieldChunk<SimpleFieldCell>> future : futures) System.out.println(future.get());

        if (chunkSet.size() == 1)
            System.out.println("correct behavior");
        else
            System.out.println("incorrect behavior");

        exec.shutdown();
    }
}
