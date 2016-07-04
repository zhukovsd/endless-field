package com.zhukovsd.experiments.concurrency.concurrentchunkproviding;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.EndlessFieldSizeConstraints;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 24.03.2016.
 */
public class ConcurrentChunkProvidingCounter {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Run N threads providing N different chunks and measure.
        // Chunks (small as possible) always generated on the fly, datasource.containsChunk always returns false

        // 6-8s w/o any synchronization

        ExecutorService exec = Executors.newCachedThreadPool();
//        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(exec);

        SimpleField field = new SimpleField(
                10000,
                new ChunkSize(1, 1),
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
                        //
                    }

                    @Override
                    public void modifyEntries(Map<CellPosition, SimpleFieldCell> entries) {
                        //
                    }
            }
        );

        long time = System.nanoTime();
        int count = 1000000;

        AtomicInteger done = new AtomicInteger();

//        ArrayList<Future<Boolean>> futures = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            exec.submit(
                    new Runnable() {
                        private int anonVar;

                        @Override
                        public void run() {
                            try {
                                try {
                                    if ((anonVar % 1000) == 0)
                                        System.out.println("before locking " +anonVar);

                                    boolean result = field.lockChunksByIds(Collections.singletonList(anonVar));

                                    if ((anonVar % 1000) == 0)
                                        System.out.println("after locking " +anonVar);

                                    if (!result)
                                        System.out.println("false");

                                    done.incrementAndGet();

    //                                return result;
                                } finally {
                                    if ((anonVar % 1000) == 0)
                                        System.out.println("before unlocking " +anonVar);

                                    field.unlockChunks();

                                    if ((anonVar % 1000) == 0)
                                        System.out.println("after unlocking " +anonVar);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        private Runnable init(int var){
                            anonVar = var;
                            return this;
                        }
                    }.init(i)
            );
        }

//        HashSet<EndlessFieldChunk<SimpleFieldCell>> chunkSet = new HashSet<>();

//        int received = 0;
//        while (received < count) {
//            Future<Boolean> resultFuture = completionService.take(); //blocks if none available
////            chunkSet.add(resultFuture.get());
//            received ++;
//        }

        while (done.get() != count) {
            System.out.println("done = " + done.get());
            TimeUnit.SECONDS.sleep(1);
        }

        time = (System.nanoTime() - time) / 1000000;

        System.out.println("time = " + time + "ms");

        exec.shutdownNow();
    }
}
