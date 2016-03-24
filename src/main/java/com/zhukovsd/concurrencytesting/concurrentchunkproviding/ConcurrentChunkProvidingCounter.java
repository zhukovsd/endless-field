package com.zhukovsd.concurrencytesting.concurrentchunkproviding;

import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
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
public class ConcurrentChunkProvidingCounter {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Run N threads proving N different chunks and measure.
        // Chunks (small as possible) always generated on the fly, datasource.containsChunk always returns false

        // 6-8s w/o any synchronization

        ExecutorService exec = Executors.newCachedThreadPool();
        CompletionService<EndlessFieldChunk<SimpleFieldCell>> completionService = new ExecutorCompletionService<>(exec);

        SimpleField field = new SimpleField(
            new ChunkSize(1, 1),
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

        long time = System.nanoTime();
        int count = 1000000;

        ArrayList<Future<EndlessFieldChunk<SimpleFieldCell>>> futures = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            futures.add(completionService.submit(
                    new Callable<EndlessFieldChunk<SimpleFieldCell>>() {
                        private int anonVar;

                        @Override
                        public EndlessFieldChunk<SimpleFieldCell> call() throws Exception {
                            return field.getChunk(anonVar);
                        }

                        private Callable<EndlessFieldChunk<SimpleFieldCell>> init(int var){
                            anonVar = var;
                            return this;
                        }
                    }.init(i)
            ));
        }

        HashSet<EndlessFieldChunk<SimpleFieldCell>> chunkSet = new HashSet<>();

        int received = 0;
        while (received < count) {
            Future<EndlessFieldChunk<SimpleFieldCell>> resultFuture = completionService.take(); //blocks if none available
            chunkSet.add(resultFuture.get());
            received ++;
        }

        time = (System.nanoTime() - time) / 1000000;

        System.out.println("time = " + time + "ms");

        exec.shutdown();
    }
}
