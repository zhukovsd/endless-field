package com.zhukovsd.experiments.concurrency.concurrentchunkdeleting;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ZhukovSD on 02.04.2016.
 */
class AllocationTest {
    static class A {
        Object createLocks() {
            ArrayList<ReentrantLock> locks = new ArrayList<>();
            for (int i = 0; i < 40000000; i++) {
                ReentrantLock lock = new ReentrantLock();
                lock.lock();
                locks.add(lock);
            }
//            locks.clear();

            return locks;
        }
    }

    public static void main(String[] args) {
        // Memory usage test. Allocation of 40kk x 2 times is possible only if resulted array is not stored and cleared
        // by garbage collector, or on 8GB+ RAM for JVM. Locked lock objects may be freed.

        new Thread(
                () -> {
                    Runtime runtime = Runtime.getRuntime();

                    while (true) {
                        try {
                            double free = 1 - ((double) runtime.freeMemory()) / ((double) runtime.totalMemory());

                            System.out.println(free);

                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
//                            System.out.println(e);
                        }
                    }
                }
        ).start();

        new A().createLocks();

        System.out.println("once more!");

        new A().createLocks();

        System.out.println("enough");
    }
}
