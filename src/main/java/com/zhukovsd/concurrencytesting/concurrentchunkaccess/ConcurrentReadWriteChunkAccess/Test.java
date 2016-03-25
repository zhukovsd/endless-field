package com.zhukovsd.concurrencytesting.concurrentchunkaccess.ConcurrentReadWriteChunkAccess;

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
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by ZhukovSD on 25.03.2016.
 */
public class Test {
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
        field.provideChunk(0);

        ExecutorService exec = Executors.newCachedThreadPool();

        int maxRow = 3, maxColumn = 3;
        ArrayList<CellPosition> positions = new ArrayList<>();

        for (int row = 0; row < maxRow; row++) {
            for (int column = 0; column < maxColumn; column++) {
                positions.add(new CellPosition(row, column));
            }
        }

        int count = 20;

        AtomicInteger readCounter = new AtomicInteger(0), writeCounter = new AtomicInteger(0);

        // writers
        for (int i = 0; i < count; i++) {
            exec.execute(new Runnable() {
                Iterable<CellPosition> positions;

                @Override
                public void run() {
                    try {
                        while (true) {
                            ReadWriteLock rwl = field.chunkMap.get(0).rwLock;

//                            field.lockChunks(positions);
                            rwl.readLock().lock();
                            try {
                                writeCounter.incrementAndGet();

                                // read
                                Iterable<SimpleFieldCell> cells = field.getCells(positions);

                                rwl.readLock().unlock();
                                rwl.writeLock().lock();
                                try {
                                    // modify
                                    for (SimpleFieldCell cell : cells) {
                                        cell.setChecked(!cell.isChecked());
                                    }

                                    rwl.readLock().lock();
                                } finally {
                                    rwl.writeLock().unlock();
                                }
                            } finally {
//                                field.unlockChunks();
                                rwl.readLock().unlock();
                            }
                        }
                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
                    }
                }

                private Runnable init(Iterable<CellPosition> positions) {
                    this.positions = positions;
                    return this;
                }
            }.init(positions));
        }

        // readers
        for (int i = 0; i < count; i++) {
            exec.execute(new Runnable() {
                Iterable<CellPosition> positions;

                @Override
                public void run() {
                    try {
                        while (true) {
//                            field.lockChunks(positions);
                            field.chunkMap.get(0).rwLock.readLock().lock();
                            try {
                                readCounter.incrementAndGet();

                                // read
                                Iterable<SimpleFieldCell> cells = field.getCells(positions);
                            } finally {
//                                field.unlockChunks();
                                field.chunkMap.get(0).rwLock.readLock().unlock();
                            }
                        }
                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
                    }
                }

                private Runnable init(Iterable<CellPosition> positions) {
                    this.positions = positions;
                    return this;
                }
            }.init(positions));
        }

        // checker
        exec.execute(
                () -> {
                    try {
                        while (true) {
                            Set<Boolean> statesSet = new HashSet<>();

//                            field.lockChunks(positions);
                            field.chunkMap.get(0).rwLock.readLock().lock();
                            try {
                                for (int row = 0; row < maxRow; row++) {
                                    for (int column = 0; column < maxColumn; column++) {
                                        statesSet.add(field.getCell(new CellPosition(row, column)).isChecked());
                                    }
                                }
                            } finally {
//                                field.unlockChunks();
                                field.chunkMap.get(0).rwLock.readLock().lock();
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

        System.out.format("read counter = %s, write counter = %s\n", readCounter.get(), writeCounter.get());

        // hi there
    }
}
