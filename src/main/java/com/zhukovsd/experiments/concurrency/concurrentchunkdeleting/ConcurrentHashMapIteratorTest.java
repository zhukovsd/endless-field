package com.zhukovsd.experiments.concurrency.concurrentchunkdeleting;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhukovSD on 05.04.2016.
 */
public class ConcurrentHashMapIteratorTest {
    public static void main(String[] args) {
        // iterate concurrent hash map and modify it after iterator() was called.
        // iteration after field changing fill affect iterator, already iterated elements may be remove after
        // being iterated.

        ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();

        ExecutorService exec = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            map.put(i, ((Integer) i).toString());
        }

        exec.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
                        System.out.println("got iterator");

//                        try {
//                            TimeUnit.SECONDS.sleep(5);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                        Integer minKey = 1;

                        int count = 0;
                        while (iterator.hasNext()) {
                            Map.Entry<Integer, String> next = iterator.next();

                            System.out.println(next.getKey() + " " + next.getValue());
                            count++;

                            if (next.getKey() < minKey) {
                                minKey = next.getKey();
                            }

                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        System.out.println("iterated over " + count + " elements");

                        if (!map.containsKey(minKey)) {
                            System.out.println("min key entry already deleted");
                        }
                    }
                }
        );

        exec.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                map.clear();
                System.out.println("map cleared");
            }
        });
    }
}
