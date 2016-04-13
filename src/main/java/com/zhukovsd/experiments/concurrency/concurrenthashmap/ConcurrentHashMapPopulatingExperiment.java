package com.zhukovsd.experiments.concurrency.concurrenthashmap;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 11.04.2016.
 */
public class ConcurrentHashMapPopulatingExperiment {
    public static void main(String[] args) throws InterruptedException {
        // concurrent hash map populated from multiple threads and simultaneously thinned out by multiple threads
        // 3 approaches tried out:
        // vanilla java SE5+ syntax, key lock manager, Java 8 syntax

        // results:

        //                     |       Java 5+       |    KeyLockManager    |        Java 8
        // --------------------|---------------------|----------------------|---------------------
        // readers count = 100 |                     |                      |
        // range = 100         | reads count = 230kk | reads count = 43kk   | reads count = 172kk
        // removers count = 2  | removes count = 2kk | removes count = 600k | removes count = 3kk
        // time = 5            |                     |                      |
        // --------------------|---------------------|----------------------|----------------------
        // readers count = 10  | reads count = 150kk | reads count = 27kk   | reads count = 115kk
        // range = 100k        | removes count = 2kk | removes count = 4kk  | removes count = 18kk
        // removers count = 2  |                     |                      |
        // time = 5            |                     |                      |

        // However, this experiment does not provide idiom for using ConcurrentHashMap item, only for multithreaded
        // map populating, because item may be removed right after creation and no locking provided to protect it during usage.

        ConcurrentHashMap<Integer, AtomicInteger> map = new ConcurrentHashMap<>();

        ExecutorService exec = Executors.newCachedThreadPool();

        int readThreadCount = 20, removeThreadCount = 0, range = 100, execTime = 5;

        AtomicInteger putCount = new AtomicInteger(0), readCount = new AtomicInteger(0), removeCount = new AtomicInteger(0);

        //region vanilla concurrent hash map populating
        for (int i = 0; i < readThreadCount; i++) {
            exec.submit((Runnable) () -> {
                Random rand = new Random();

                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Integer key = rand.nextInt(range);

                        if (map.putIfAbsent(key, new AtomicInteger()) == null)
                            putCount.incrementAndGet();

                        AtomicInteger getResult = map.get(key); // may be null!
                        if (getResult != null)
                            getResult.incrementAndGet();

//                        if (getResult == null)
//                            System.out.println("null");

                        readCount.incrementAndGet();

//                        System.out.println(getResult);
//                        break;
                    }
                } catch (Exception e) {
                    System.out.println("interrupted");
                }
            });
        }

        for (int i = 0; i < removeThreadCount; i++) {
            exec.submit((Runnable) () -> {
                Random rand = new Random();

                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Integer key = rand.nextInt(range);

                        Object value = map.get(key);
                        if (value != null) {
                            boolean removed = false;

                            if (map.remove(key, value))
                                removeCount.incrementAndGet();

//                            while (!removed) {
//                                if (map.remove(key, value)) {
//                                    removeCount.incrementAndGet();
//                                    removed = true;
//                                } else {
//                                    value = map.get(key);
//                                }
//                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("interrupted");
                }
            });
        }
        //endregion

        //region key lock manager
//        KeyLockManager klm = KeyLockManagers.newLock();
//        for (int i = 0; i < readThreadCount; i++) {
//            exec.submit((Runnable) () -> {
//                Random rand = new Random();
//
//                try {
//                    while (!Thread.currentThread().isInterrupted()) {
//                        Integer key = rand.nextInt(range);
//
//                        Object getResult = klm.executeLocked(
//                                key, () -> {
//                                    if (map.containsKey(key))
//                                        return map.get(key);
//                                    else {
//                                        Object object = new Object();
//                                        map.put(key, object);
//                                        putCount.incrementAndGet();
//
//                                        return object;
//                                    }
//                                }
//                        );
//                        readCount.incrementAndGet();
////                    System.out.format("#%s get = %s\n", index, getResult);
//                    }
//                } catch (Exception e) {
////                    System.out.println("interrupted");
//                }
//            });
//        }
//
//        for (int i = 0; i < removeThreadCount; i++) {
//            exec.submit((Runnable) () -> {
//                Random rand = new Random();
//
//                try {
//                    while (!Thread.currentThread().isInterrupted()) {
//                        Integer key = rand.nextInt(range);
//
//                        klm.executeLocked(
//                                key, () -> {
//                                    if (map.containsKey(key)) {
//                                        map.remove(key);
//                                        removeCount.incrementAndGet();
//                                    }
//                                }
//                        );
////                    System.out.format("#%s get = %s\n", index, getResult);
//                    }
//                } catch (Exception e) {
////                    System.out.println("interrupted");
//                }
//            });
//        }
        //endregion

        //region Java 8
//        for (int i = 0; i < readThreadCount; i++) {
//            exec.submit((Runnable) () -> {
//                Random rand = new Random();
//
//                try {
//                    while (!Thread.currentThread().isInterrupted()) {
//                        Integer key = rand.nextInt(range);
//
//                        map.computeIfAbsent(key, (k) -> {
//                            putCount.incrementAndGet();
//                            return new Object();
//                        });
//
//                        readCount.incrementAndGet();
//                    }
//                } catch (Exception e) {
////                    System.out.println("interrupted");
//                }
//            });
//        }
//
//        for (int i = 0; i < removeThreadCount; i++) {
//            exec.submit((Runnable) () -> {
//                Random rand = new Random();
//
//                try {
//                    while (!Thread.currentThread().isInterrupted()) {
//                        Integer key = rand.nextInt(range);
//
//                        Object value = map.get(key);
//                        if (value != null) {
//                            boolean removed = false;
//
//                            while (!removed) {
//                                if (map.remove(key, value)) {
//                                    removeCount.incrementAndGet();
//                                    removed = true;
//                                } else {
//                                    value = map.get(key);
//                                }
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    System.out.println("interrupted");
//                }
//            });
//        }
        //endregion

        TimeUnit.SECONDS.sleep(5);
        exec.shutdownNow();

        System.out.printf("read count = %s, put count = %s, remove count = %s\n", readCount, putCount, removeCount);
        System.out.printf("map size = put count - remove count, %s = %s - %s - %s\n", map.size(), putCount, removeCount, (map.size() == putCount.get() - removeCount.get()));

//        int c = 0;
//        for (AtomicInteger atomicInteger : map.values()) {
//            c += atomicInteger.get();
//        }
//        System.out.println("c = " + c);
    }
}
