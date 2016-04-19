package com.zhukovsd.experiments.concurrency;

import com.zhukovsd.enrtylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
import com.zhukovsd.enrtylockingconcurrenthashmap.Lockable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ZhukovSD on 18.04.2016.
 */
public class ConcurrentUserScopesExperiment {
    static class LockableHashSetAdapter<E> extends HashSet<E> implements Lockable {
        static AtomicInteger lockCount = new AtomicInteger(), unlockCount = new AtomicInteger();

        ReentrantLock lock = new ReentrantLock();

        @Override
        public void lockInterruptibly() throws InterruptedException {
            lock.lockInterruptibly();

            lockCount.incrementAndGet();
        }

        @Override
        public void unlock() {
            lock.unlock();
            unlockCount.incrementAndGet();
        }

        @Override
        public String test() {
            return lock.toString();
        }

        @Override
        public boolean isLocked() {
            return lock.isLocked();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Map<userId, scope>
        final ConcurrentHashMap<Integer, Set<Integer>> scopes = new ConcurrentHashMap<>();
        // Map<chunkId, users>
//        final ConcurrentHashMap<Integer, Set<Integer>> usersScopeMap = new ConcurrentHashMap<>();
        final EntryLockingConcurrentHashMap<Integer, LockableHashSetAdapter<Integer>> usersScopeMap = new EntryLockingConcurrentHashMap<Integer, LockableHashSetAdapter<Integer>>() {
            @Override
            protected LockableHashSetAdapter<Integer> instantiateValue(Integer key) {
                return new LockableHashSetAdapter<>();
            }
        };

        ExecutorService exec = Executors.newCachedThreadPool();

        // default concurrent hash maps (20, 5, 10, 100, 3) -> count = 4.6kk, iterCount = 90kk
        int threadCount = 20, iteratorThreadCount = 0, userCount = 10, range = 10, maxScopeSize = 3;
        AtomicInteger count = new AtomicInteger(), iterCount = new AtomicInteger(), doneCount = new AtomicInteger(),
                removeLockCount = new AtomicInteger(), addLockCount = new AtomicInteger();

        Object checkLock = new Object();

        for (int i = 0; i < userCount; i++)
            scopes.put(i, new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            exec.submit(() -> {
                Random rand = new Random();

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Integer userId = rand.nextInt(userCount);

                        Set<Integer> scope = scopes.get(userId);

                        // generate new scope
                        Set<Integer> newScope = new HashSet<>();
                        for (int j = 0; j < maxScopeSize; j++) {
                            newScope.add(rand.nextInt(range));
                        }

                        synchronized (scope) {
                            // remove current scope keys from chunk id set
                            int a = scope.size();
                            for (Integer key : scope) {
                                int b = scope.size();
                                if (a != b)
                                    System.out.println("hi there");

//                                Set<Integer> users = usersScopeMap.putIfAbsent(key, ConcurrentHashMap.newKeySet());
//                                if (users == null)
//                                    users = usersScopeMap.get(key);

//                                if (users != null) {
//                                    users.remove(userId);
//                                } else {
//                                    System.out.println(1);
//                                }

                                Set<Integer> users = usersScopeMap.lockKey(key);
                                try {
                                    users.remove(userId);
                                    Thread.yield();
                                } finally {
                                    usersScopeMap.unlock();
                                }
                                removeLockCount.incrementAndGet();
                            }

                            // update scope
                            scope.clear();
                            scope.addAll(newScope);

                            // put updated scope keys to chunk id set
                            for (Integer key : scope) {
//                                Set<Integer> users = usersScopeMap.putIfAbsent(key, ConcurrentHashMap.newKeySet());
//                                if (users == null)
//                                    users = usersScopeMap.get(key);
//
//                                if (users != null) {
//                                    users.add(userId);
//                                } else {
//                                    System.out.println(1);
//                                }

                                Set<Integer> users = usersScopeMap.lockKey(key);
                                try {
                                    users.add(userId);
                                    Thread.yield();
                                } finally {
                                    usersScopeMap.unlock();
                                }
                                addLockCount.incrementAndGet();
                            }
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

//                        Set<Integer> users = usersScopeMap.get(chunkId);
//
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

        TimeUnit.SECONDS.sleep(3);
        exec.shutdownNow();
        TimeUnit.SECONDS.sleep(1); // ! have to wait for all threads to complete

        System.out.println("doneCount = " + doneCount + ", addLockCount = " + addLockCount + ", removeLockCount = " + removeLockCount);
        System.out.println("lock count = " + LockableHashSetAdapter.lockCount + ", unlock count = " + LockableHashSetAdapter.unlockCount);

        System.out.println("scopes = " + scopes);
        System.out.println("scope map  = " + usersScopeMap);

        boolean isCorrect = true;
        for (Map.Entry<Integer, Set<Integer>> scopeEntry : scopes.entrySet()) {
            Integer userId = scopeEntry.getKey();
            Set<Integer> chunks = scopeEntry.getValue();

            for (Integer chunk : chunks) {
                Set<Integer> users = usersScopeMap.lockKey(chunk);
                try {
                    if (users != null) {
                        if (users.contains(userId))
                            users.remove(userId);
                        else
                            isCorrect = false;
                    }
                } finally {
                    usersScopeMap.unlock();
                }
            }
        }

        // TODO: 18.04.2016 proper iteration
        for (Set<Integer> users : usersScopeMap.map.values()) {
            if (users.size() != 0)
                isCorrect = false;
        }

        System.out.println("isCorrect = " + isCorrect);

        System.out.println("count = " + count);
        System.out.println("iterCount = " + iterCount);
    }
}
