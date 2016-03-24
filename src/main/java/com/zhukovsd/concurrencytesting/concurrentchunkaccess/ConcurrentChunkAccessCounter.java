package com.zhukovsd.concurrencytesting.concurrentchunkaccess;

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
import java.util.concurrent.atomic.AtomicInteger;

class ReadTask implements Runnable {
    static AtomicInteger counter = new AtomicInteger(0);

    SimpleField field;
    int chunkId;

    ReadTask(SimpleField field, Integer chunkId) {
        this.field = field;
        this.chunkId = chunkId;
    }

    @Override
    public void run() {
        try {
            while (true) {
                field.lockChunk(chunkId);
                try {
                    counter.incrementAndGet();
                } finally {
                    field.unlockChunk();
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
public class ConcurrentChunkAccessCounter {
    public static void main(String[] args) throws InterruptedException {
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

        int maxRow = 3, maxColumn = 3;
        ArrayList<SimpleFieldCell> cells = new ArrayList<>();

        for (int row = 0; row < maxRow; row++) {
            for (int column = 0; column < maxColumn; column++) {
                cells.add(field.getCell(new CellPosition(row, column)));
            }
        }


        int count = 20;

        // writers
        for (int i = 0; i < count; i++) exec.execute(new WriteTask(field, cells));

        // readers
        for (int i = 0; i < count; i++) exec.execute(new ReadTask(field, 0));

        TimeUnit.SECONDS.sleep(10);
        exec.shutdownNow();

        System.out.format("read counter = %s, write counter = %s\n", ReadTask.counter, WriteTask.counter);
    }
}
