package com.zhukovsd.experiments.concurrency.concurrenthashmap;

import com.zhukovsd.enrtylockingconcurrenthashmap.AbstractLockable;
import com.zhukovsd.enrtylockingconcurrenthashmap.EntryLockingConcurrentHashMap;

import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

class LockableString extends AbstractLockable {
    // protected by lock
    String s;

    // protected by lock
    int c = 0;

    public LockableString(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return s;
    }
}

class TestEntryLockingConcurrentHashMap extends EntryLockingConcurrentHashMap<Integer, LockableString> { }

public class ConcurrentHashMapItemLockingExperiment {
    public static void main(String[] args) throws InterruptedException {
        TestEntryLockingConcurrentHashMap map = new TestEntryLockingConcurrentHashMap();
        ExecutorService exec = Executors.newCachedThreadPool();

        // (20, 0, 100) -> c = 30kk
        int readersCount = 20, removersCount = 0, range = 100;
        AtomicInteger readCount = new AtomicInteger(), removeCount = new AtomicInteger();

        final Function<Integer, LockableString> producer = integer -> new LockableString("value = " + integer);

        for (int i = 0; i < readersCount; i++) {
            exec.submit((Runnable) () -> {
                try {
                    Random rand = new Random();

                    while (!Thread.currentThread().isInterrupted()) {
//                        LinkedHashMap<Integer, LockableString> entries = map.lockEntries(Collections.singletonList(rand.nextInt(range)));
                        int key = rand.nextInt(range);
                        if (map.lockKey(key, producer)) {
                            try {
                                map.getValue(key).c++;

                                readCount.incrementAndGet();
                            } finally {
                                map.unlock();
                            }
                        }
                    }
                } catch (Exception e) {
//                    System.out.println(e);
                }
            });
        }

        for (int i = 0; i < removersCount; i++) {
            exec.submit((Runnable) () -> {
                try {
                    Random rand = new Random();

                    while (!Thread.currentThread().isInterrupted()) {
                        Set<Map.Entry<Integer, LockableString>> set = map.getEntriesNonLocked();

                        if (set.size() > 0) {
                            ArrayList<Map.Entry<Integer, LockableString>> randomizedList = new ArrayList<>(set);
                            Collections.shuffle(randomizedList, rand);

                            Map.Entry<Integer, LockableString> entry = randomizedList.get(0);

                            map.remove(entry.getKey());

                            removeCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
//                    System.out.println("hi there");
                }
            });
        }

        TimeUnit.SECONDS.sleep(5);
        exec.shutdownNow();

        System.out.printf("read count = %s, remove count = %s\n", readCount, removeCount);

        int sum = 0;
        for (Map.Entry<Integer, LockableString> entry : map.getEntriesNonLocked()) {
            sum += entry.getValue().c;
        }

        System.out.println("c = " + sum);
    }
}
