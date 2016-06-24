package com.zhukovsd.experiments.performance;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 13.04.2016.
 */
public class HashSetVsConcurrentHashSetExperiment {
    public static void main(String[] args) {
        Set<Integer> concurrentHashSet = ConcurrentHashMap.newKeySet();
        Set<Integer> hashSet = new HashSet<>();

        ExecutorService exec = Executors.newCachedThreadPool();

        AtomicInteger c1 = new AtomicInteger(), c2 = new AtomicInteger();
        int range = 100;

        exec.submit(() -> {
            Random rand = new Random();

            while (!Thread.currentThread().isInterrupted()) {
                if (rand.nextInt(10) != 0)
                    concurrentHashSet.add(rand.nextInt(100));
                else
                    concurrentHashSet.remove(rand.nextInt(100));

                c1.incrementAndGet();
            }
        });

        exec.submit(() -> {
            Random rand = new Random();

            while (!Thread.currentThread().isInterrupted()) {
                if (rand.nextInt(10) != 0)
                    hashSet.add(rand.nextInt(100));
                else
                    hashSet.remove(rand.nextInt(100));

                c2.incrementAndGet();
            }
        });

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        exec.shutdownNow();

        System.out.println("c1 = " + c1);
        System.out.println("c2 = " + c2);
    }
}
