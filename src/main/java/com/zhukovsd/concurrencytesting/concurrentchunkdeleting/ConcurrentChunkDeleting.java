package com.zhukovsd.concurrencytesting.concurrentchunkdeleting;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.fielddatasource.StoreChunkTask;
import com.zhukovsd.endlessfield.fielddatasource.UpdateCellTask;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;
import com.zhukovsd.simplefield.SimpleFieldDataSource;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 01.04.2016.
 */
public class ConcurrentChunkDeleting {
    public static void main(String[] args) throws InterruptedException {
        ChunkSize chunkSize = new ChunkSize(50, 50);
        final SimpleField field = new SimpleField(chunkSize, new SimpleFieldDataSource(), new SimpleFieldCellFactory());

        ThreadPoolExecutor chunkStoreExec = ((ThreadPoolExecutor) field.chunkStoreExec);
        ThreadPoolExecutor cellUpdateExec = ((ThreadPoolExecutor) field.cellUpdateExec);

        ExecutorService readers = Executors.newCachedThreadPool();
//        new ThreadFactory() {
//            @Override
//            public Thread newThread(Runnable r) {
//                Thread thread = new Thread(r, "reading thread");
//                return thread;
//            }
//        });

        ExecutorService watchers = Executors.newSingleThreadExecutor();
//        new ThreadFactory() {
//            @Override
//            public Thread newThread(Runnable r) {
//                Thread thread = new Thread(r, "removing thread");
//                return thread;
//            }
//        });

        ExecutorService remover = Executors.newSingleThreadExecutor();

        int chunkCount = 40;
        int maxAllowedCount = 1500;
        int maxRow = chunkCount * chunkSize.rowCount;
        int maxColumn = chunkCount * chunkSize.columnCount;
//        int maxRow = 1;
//        int maxColumn = 1;

        int readersCount = 5;

        // 0 - remover state, 1 - remove count, 2 - write count
        final AtomicInteger[] a = new AtomicInteger[]{new AtomicInteger(0), new AtomicInteger(0)};

        for (int i = 0; i < readersCount; i++) {
            readers.submit((Runnable) () -> {
                Random rand = new Random();

                try {
                    while (true) {
                        List<CellPosition> positions = Arrays.asList(new CellPosition(rand.nextInt(maxRow), rand.nextInt(maxColumn)));

//                        List<CellPosition> positions = new ArrayList<>();
//                        for (int row = 0; row < 50; row++) {
//                            for (int column = 0; column < 50; column++) {
//                                positions.add(new CellPosition(row, column));
//                            }
//                        }

                        field.lockChunks(positions);
                        try {
                            a[1].incrementAndGet();

                            LinkedHashMap<CellPosition, SimpleFieldCell> entries = field.getEntries(positions);
                            for (Map.Entry<CellPosition, SimpleFieldCell> entry : entries.entrySet()) {
                                SimpleFieldCell cell = entry.getValue();

                                if (cell == null)
                                    System.out.println("123456");

                                synchronized (cell) {
                                    cell.setChecked(!cell.isChecked());
                                }
                            }

                            field.updateEntries(entries);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                field.unlockChunks();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

//                        try {
//                            TimeUnit.MILLISECONDS.sleep(5);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        watchers.submit((Runnable) () -> {
            int counter = 0;
            while (true) {
                try {
                    System.out.format("#%s chunk count = %s/%s, read count = %s, remove count = %s, store queue size = %s, chunk store count = %s" +
                            ", update queue size = %s, update count = %s\n",
                            counter, field.chunkMap.size(), chunkCount * chunkCount, a[1], a[0],
                            chunkStoreExec.getQueue().size(), StoreChunkTask.storeCount,
                            cellUpdateExec.getQueue().size(), UpdateCellTask.updateCount
                    );

                    counter++;
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        remover.submit((Runnable)  () -> {
            while (true) {
                try {
//                   int c = 0;

//                   if (field.chunkMap.size() > 0) {
                    while (field.chunkMap.size() > maxAllowedCount) {
                        Random rand = new Random();

                        ArrayList<Integer> keys = new ArrayList<>(field.chunkMap.keySet());

                        Integer key = keys.get(rand.nextInt(keys.size()));

                        if (field.chunkMap.get(key).isStored() && (field.chunkMap.get(key).updateTaskCount.get() == 0)) {
                            field.removeChunk(key);
                            a[0].incrementAndGet();
                        }

                        Thread.yield();
                    }
//                   }


//                   if (field.chunkMap.size() > maxAllowedCount) {
//                       Iterator<Map.Entry<Integer, EndlessFieldChunk<SimpleFieldCell>>> iterator = field.chunkMap.entrySet().iterator();
//
//                       while (iterator.hasNext()) {
//                           iterator.next();
//                           c++;
//
//                           Thread.yield();
//                       }
////                   }
//
//                   System.out.printf("--> iterated count = %s, chunks count = %s\n", c, field.chunkMap.size());
//
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

//        readers.submit((Runnable) () -> {
//            field.removeChunk(0);
//            System.out.println("chunk #" + 0 + " removed from field");
//        });
    }
}

