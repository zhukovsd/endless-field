package com.zhukovsd.concurrencytesting.concurrentcellupdating;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.fielddatasource.UpdateCellTask;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;
import com.zhukovsd.simplefield.SimpleFieldDataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 30.03.2016.
 */
public class ConcurrentCellUpdating {
    public static void main(String[] args) throws InterruptedException {
        SimpleField field = new SimpleField(new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory());

        ExecutorService updateExec = Executors.newCachedThreadPool();
        ExecutorService watcherExec = Executors.newSingleThreadExecutor();

        int count = 999;

        AtomicInteger c = new AtomicInteger(0);

        for (int i = 0; i < count; i++) {
            updateExec.submit((Runnable) () -> {
                try {
                    ArrayList<CellPosition> positions = new ArrayList<>(Arrays.asList(new CellPosition(0, 0)));

                    while (true) {
                        field.lockChunks(positions);
                        try {
                            SimpleFieldCell cell = field.getCell(positions.get(0));
                            try {
                                synchronized (cell) {
                                    cell.setChecked(!cell.isChecked());

                                    cell.incA();
                                    cell.incB();
                                }
                            } finally {
                                c.incrementAndGet();
                                field.updateCell(positions.get(0), cell);
                            }
                        } finally {
                            field.unlockChunks();
                        }
                    }
                } catch (Exception e) {
                    //
                }
            });
        }

        watcherExec.submit((Runnable) () -> {
            while (true) {
                System.out.println(
                        "queue size = " + ((ThreadPoolExecutor) field.cellUpdateExec).getQueue().size()
                                + ", pool size = " + ((ThreadPoolExecutor) field.cellUpdateExec).getPoolSize()
                                + ", counters = "
                                + SimpleFieldDataSource.runCounter.get() + ", " + c.get()
                );
//                System.out.println(((double) SimpleFieldDataSource.loopCounter.get()) / ((double) SimpleFieldDataSource.runCounter.get())
//                        + ", " + ((double) SimpleFieldDataSource.obsoleteCounter.get()) / ((double) SimpleFieldDataSource.runCounter.get())
//                );

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        TimeUnit.SECONDS.sleep(10);

//        System.out.println(SimpleFieldDataSource.runCounter.get() + ", " + c.get());

        System.out.println("shutting down update thread");
        updateExec.shutdownNow();
    }
}
