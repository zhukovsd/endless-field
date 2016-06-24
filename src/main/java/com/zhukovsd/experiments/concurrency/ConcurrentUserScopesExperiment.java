package com.zhukovsd.experiments.concurrency;

import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
//import com.zhukovsd.enrtylockingconcurrenthashmap.KLMEntryLockingConcurrentHashMap;
//import com.zhukovsd.enrtylockingconcurrenthashmap.StripedEntryLockingConcurrentHashMap;
//import com.zhukovsd.serverapp.cache.scopes.LockableConcurrentHashSetAdapter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Created by ZhukovSD on 18.04.2016.
 */
public class ConcurrentUserScopesExperiment {
//    private static LockableConcurrentHashSetAdapter<Integer> producer(Integer key) {
//        return new LockableConcurrentHashSetAdapter<>();
//    }

    public static void main(String[] args) throws InterruptedException {
        // Map<userId, scope>
        final ConcurrentHashMap<Integer, Set<Integer>> scopes = new ConcurrentHashMap<>();
        // Map<chunkId, users>
//        final ConcurrentHashMap<Integer, Set<Integer>> usersScopeMap = new ConcurrentHashMap<>();
//        final EntryLockingConcurrentHashMap<Integer, LockableConcurrentHashSetAdapter<Integer>> usersScopeMap
//                 = new KLMEntryLockingConcurrentHashMap<>();
//                = new StripedEntryLockingConcurrentHashMap<>(500);

        ExecutorService exec = Executors.newCachedThreadPool();
        long startTime = System.nanoTime();

        // default concurrent hash maps (20, 5, 10, 100, 3, 5) -> count = 4.6kk, iterCount = 90kk
        // entry locking concurrent hash map (20, 5, 10, 100, 3, 5) -> count = 1.2-1.5kk, iterCount = 50-70kk, 4kk removes
        // striped(range) locking concurrent hash map (20, 5, 10, 100, 3, 5) -> count = 3-3.5kk, iterCount = 50-60kk , remove count = 6-7kk
        int threadCount = 200, iteratorThreadCount = 5, userCount = 1000, range = 15000, maxScopeSize = 3, time = 5;
        AtomicInteger count = new AtomicInteger(), removeCount = new AtomicInteger(), iterCount = new AtomicInteger(),
                doneCount = new AtomicInteger(), removeLockCount = new AtomicInteger(), addLockCount = new AtomicInteger();

        for (int i = 0; i < userCount; i++)
            scopes.put(i, new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            exec.submit(() -> {
                Random rand = new Random();

                while (!Thread.currentThread().isInterrupted()) {
                    try {
//                        long msFromStart = (System.nanoTime() - startTime) / 1000000;
//                        long msLeft = time * 1000 - msFromStart;
//                        if (msLeft < 100)
//                            TimeUnit.SECONDS.sleep(1);

                        Integer userId = rand.nextInt(userCount);

                        Set<Integer> scope = scopes.get(userId);

                        // generate new scope
                        Set<Integer> newScope = new HashSet<>();
                        for (int j = 0; j < maxScopeSize; j++) {
                            newScope.add(rand.nextInt(range));
                        }

                        // lock due to its changing
                        synchronized (scope) {
                            // remove current scope keys from chunk id set
                            for (Integer key : scope) {
//                                if (usersScopeMap.lockKey(key)) {
//                                    Set<Integer> users = usersScopeMap.getValue(key);
//                                    try {
//                                        users.remove(userId);
//                                    } finally {
//                                        usersScopeMap.unlock();
//                                    }
//                                    removeLockCount.incrementAndGet();
                                }

//                                final Function<LockableConcurrentHashSetAdapter<Integer>, Boolean> function = (integers ->
//                                        (integers.size() == 0) // && (usersScopeMap.size() > 50)
//                                );

                                // item removal possible only after unlocking
                                // remove if, only is value set is empty
//                                if (usersScopeMap.removeIf(key, function))
//                                    removeCount.incrementAndGet();
                            }

                            // update scope
                            scope.clear();
                            scope.addAll(newScope);

                            // put updated scope keys to chunk id set
                            for (Integer key : scope) {
//                                if (usersScopeMap.lockKey(key, ConcurrentUserScopesExperiment::producer)) {
//                                    Set<Integer> users = usersScopeMap.getValue(key);
//                                    try {
//                                        users.add(userId);
//                                    } finally {
//                                        usersScopeMap.unlock();
//                                    }
//                                    addLockCount.incrementAndGet();
//                                }
//                            }
                        }

                        count.incrementAndGet();
                    } catch (Exception e) {
                        if (!(e instanceof InterruptedException))
                            e.printStackTrace();
                        else
                            Thread.currentThread().interrupt(); // handle interrupted exception and set isInterrupted flag to exit the loop
                    }
                }

//                System.out.println(Thread.currentThread().getName() + " out");
                doneCount.incrementAndGet();
            });
        }

        for (int i = 0; i < iteratorThreadCount; i++) {
            exec.submit(() -> {
                Random rand = new Random();

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Integer chunkId = rand.nextInt(range);

//                        Set<Integer> users = usersScopeMap.getValueNonLocked(chunkId);
//                        if (users != null) {
//                            for (Integer user : users) {
//                                int a = user + 1; // do nothing
//                            }
//
//                            iterCount.incrementAndGet();
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        TimeUnit.SECONDS.sleep(time);
        exec.shutdownNow();
        TimeUnit.SECONDS.sleep(1); // ! have to wait for all threads to complete

        System.out.println("doneCount = " + doneCount + ", addLockCount = " + addLockCount + ", removeLockCount = " + removeLockCount);
//        System.out.println("lock count = " + LockableConcurrentHashSetAdapter.lockCount + ", unlock count = " + LockableConcurrentHashSetAdapter.unlockCount);

        System.out.println("scopes = " + scopes);
//        System.out.println("scope map  = " + usersScopeMap);

        boolean isCorrect = true;
        for (Map.Entry<Integer, Set<Integer>> scopeEntry : scopes.entrySet()) {
            Integer userId = scopeEntry.getKey();
            Set<Integer> chunks = scopeEntry.getValue();

//            for (Integer chunk : chunks) {
//                if (usersScopeMap.lockKey(chunk)) {
//                    LockableConcurrentHashSetAdapter<Integer> users = usersScopeMap.getValue(chunk);
//                    try {
//                        if (users != null) {
//                            if (users.contains(userId))
//                                users.remove(userId);
//                            else
//                                isCorrect = false;
//                        } else
//                            System.out.println("nope");
//                    } finally {
//                        usersScopeMap.unlock();
//                    }
//                }
//            }
        }

//        for (Map.Entry<Integer, LockableConcurrentHashSetAdapter<Integer>> entry : usersScopeMap.getEntriesNonLocked()) {
//            if (entry.getValue().size() != 0)
//                isCorrect = false;
//        }

        System.out.println("isCorrect = " + isCorrect);

        System.out.println("count = " + count + ", remove count = " + removeCount);
        System.out.println("iterCount = " + iterCount);
    }
}
