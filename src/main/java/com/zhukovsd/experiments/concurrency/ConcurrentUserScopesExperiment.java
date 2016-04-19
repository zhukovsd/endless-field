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
import java.util.function.Function;

/**
 * Created by ZhukovSD on 18.04.2016.
 */
public class ConcurrentUserScopesExperiment {
//    static class LockableHashSetAdapter<E> extends HashSet<E> implements Lockable {
    static class LockableHashSetAdapter<E> implements Lockable, Set<E> {
        Set<E> set = ConcurrentHashMap.newKeySet();
        ReentrantLock lock = new ReentrantLock();

        @Override
        public ReentrantLock getLock() {
            return lock;

//            Set<Integer> a = ConcurrentHashMap.newKeySet()
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return set.iterator();
        }

        @Override
        public Object[] toArray() {
            return set.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }

        @Override
        public boolean add(E e) {
            return set.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return set.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return set.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return set.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return set.removeAll(c);
        }

        @Override
        public void clear() {
            set.clear();
        }

        @Override
        public boolean equals(Object o) {
            return set.equals(o);
        }

        @Override
        public int hashCode() {
            return set.hashCode();
        }

        @Override
        public Spliterator<E> spliterator() {
            return set.spliterator();
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
        long startTime = System.nanoTime();

        // default concurrent hash maps (20, 5, 10, 100, 3) -> count = 4.6kk, iterCount = 90kk
        int threadCount = 20, iteratorThreadCount = 5, userCount = 10, range = 10, maxScopeSize = 3, time = 3;
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


//                                    if (users.size() == 0) {
//                                        isRemoved = usersScopeMap.removeLocked(key);
//                                    }
//                                      Thread.yield();
                                } finally {
                                    usersScopeMap.unlock();
                                }
                                removeLockCount.incrementAndGet();

                                final Function<LockableHashSetAdapter<Integer>, Boolean> function = (integers ->
                                    integers.size() == 0
                                );

                                if (usersScopeMap.removeIf(key, function))
                                    removeCount.incrementAndGet();
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
//                                    Thread.yield();
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

                        Set<Integer> users = usersScopeMap.getNonLocked(chunkId);
                        if (users != null) {
                            for (Integer user : users) {
                                int a = user + 1; // do nothing
                            }

                            iterCount.incrementAndGet();
                        }
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
        for (Map.Entry<Integer, LockableHashSetAdapter<Integer>> entry : usersScopeMap.entrySet()) {
            if (entry.getValue().size() != 0)
                isCorrect = false;
        }

        System.out.println("isCorrect = " + isCorrect);

        System.out.println("count = " + count + ", remove count = " + removeCount);
        System.out.println("iterCount = " + iterCount);
    }
}
