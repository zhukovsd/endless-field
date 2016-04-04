package com.zhukovsd.concurrencytesting.concurrentchunkaccess;

import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantlocks.ReentrantLockCheckTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantlocks.ReentrantLockReadTask;
import com.zhukovsd.concurrencytesting.concurrentchunkaccess.threads.reentrantlocks.ReentrantLockWriteTask;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;

import java.util.Map;
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

        // Locking tested on 1000x1000 field of 50x50 chunks with 20 writers (read + modify data) / 20 readers (only read)
        // threads. Threads accessed same or random chunks and performed its actions on given amount of cells.
        // Reads and modifies count were recorded for given time (10 seconds). Bigger count is better,
        // performance varies from locking strategy.

        //                                  amount of cells to process / results - (read count / modify count / overall count)
        // ACCESS TO SINGLE CHUNK      1x1                  3x3              50x50              100x100
        // reentrant lock       | 10kk/10kk/20kk    | 5kk/5kk/10kk      | 44k/44k/88k   | 9.5k/9.5/19k
        // !reentrant readwrite | 21kk/3.5kk/24.5kk | 7kk/2.5kk/9.5kk   | 350k/6k/356k  | 65k/4k/69k
        // !stamped             | 15kk/15kk/30kk    | 11kk/3kk/14kk     | 341k/10k/351k |

        //                                  amount of cells to process / results - (read count / modify count / overall count)
        // ACCESS TO MULTIPLE CHUNKS    1x1             3x3                  50x50         100x100
        // reentrant lock       | 24kk/24kk/48kk | 12kk/12kk/24kk      | 88k/88k/176k  | 21.5k/20.7k/42.3k
        // !reentrant readwrite | 24kk/24kk/48kk | 12kk/12kk/24kk      | 117k/68k/185k | 30k/15k/45k
        // !stamped             |                MULTIPLE CHUNKS LOCKING NOT IMPLEMENTED

        // Readwrite and stamped lock tests not entirely correct. While using this locks data may be changed between
        // read unlock and write lock, which needs additional data verification. In real life write performance
        // will be even worse.

        // Conclusions: in situation, where reads/writes amounts roughly equals, no reason to implement
        // more sophisticated locking using ReentrantReadWriteLock / StampedLock. Tests, which uses those locks,
        // in not entirely correct.

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

                    @Override
                    public void modifyEntries(Map<CellPosition, SimpleFieldCell> entries) {

                    }
                },
                new SimpleFieldCellFactory()
        );

        ExecutorService exec = Executors.newCachedThreadPool();

        int maxPosition = 50, range = 50;

        field.provideAndLockChunk(0);
//        field.unlockChunks();

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
