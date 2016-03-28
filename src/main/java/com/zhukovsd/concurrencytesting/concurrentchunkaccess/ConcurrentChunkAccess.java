package com.zhukovsd.concurrencytesting.concurrentchunkaccess;

import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantlocks.ReentrantLockCheckTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantlocks.ReentrantLockReadTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantlocks.ReentrantLockWriteTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantreadwritelocks.ReentrantReadWriteLockCheckTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantreadwritelocks.ReentrantReadWriteLockReadTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantreadwritelocks.ReentrantReadWriteLockWriteTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.stampedlocks.StampedLockCheckTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.stampedlocks.StampedLockReadTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.stampedlocks.StampedLockWriteTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhukovSD on 24.03.2016.
 */
public class ConcurrentChunkAccess {
    public static void main(String[] args) throws InterruptedException {
        // Run N threads which modifies cells in the same chunk and N threads which reads cells from it
        // Measure reads/writes count. Synchronization strategy affects read/write performance
        // Check if no race condition occurred and cell states are consistent

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

        ExecutorService exec = Executors.newCachedThreadPool();

        int maxPosition = 3, range = 3;

        field.provideChunk(0);

        int writersCount = 20, readersCount = 20;

        // writers
        for (int i = 0; i < writersCount; i++) exec.execute(new ReentrantLockWriteTask(field, range, maxPosition));
//        for (int i = 0; i < writersCount; i++) exec.execute(new ReentrantReadWriteLockWriteTask(field, range, maxPosition));
//        for (int i = 0; i < writersCount; i++) exec.execute(new StampedLockWriteTask(field, range, maxPosition));

        // readers
        for (int i = 0; i < readersCount; i++) exec.execute(new ReentrantLockReadTask(field, range, maxPosition));
//        for (int i = 0; i < readersCount; i++) exec.execute(new ReentrantReadWriteLockReadTask(field, range, maxPosition));
//        for (int i = 0; i < readersCount; i++) exec.execute(new StampedLockReadTask(field, range, maxPosition));

        // checker
        exec.execute(new ReentrantLockCheckTask(field, range, maxPosition));
//        exec.execute(new ReentrantReadWriteLockCheckTask(field, range, maxPosition));
//        exec.execute(new StampedLockCheckTask(field, range, maxPosition));

        TimeUnit.SECONDS.sleep(10);
        exec.shutdownNow();

        int readCount = ReentrantLockReadTask.counter.get();
        int writeCount = ReentrantLockWriteTask.counter.get();
//        int readCount = ReentrantReadWriteLockReadTask.counter.get();
//        int writeCount = ReentrantReadWriteLockWriteTask.counter.get();
//        int readCount = StampedLockReadTask.counter.get();
//        int writeCount = StampedLockWriteTask.counter.get();

        System.out.format(
                "read counter = %s, write counter = %s, overall = %s\n", readCount, writeCount, readCount + writeCount
        );

//        System.out.println("optimistic read = " + StampedLockReadTask.optimisticCounter.get());
//        System.out.println("optimistic read before modify = " + StampedLockWriteTask.optimisticCounter.get());
    }
}
