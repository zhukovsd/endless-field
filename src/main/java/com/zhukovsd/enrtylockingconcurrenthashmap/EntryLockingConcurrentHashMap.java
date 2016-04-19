package com.zhukovsd.enrtylockingconcurrenthashmap;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;
import de.jkeylockmanager.manager.exception.KeyLockManagerInterruptedException;

import java.lang.ref.Reference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
public abstract class EntryLockingConcurrentHashMap<K, V extends Lockable> {
    private ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private KeyLockManager lockManager = KeyLockManagers.newLock();

    private ThreadLocal<TreeSet<K>> lockedKeys = new ThreadLocal<TreeSet<K>>() {
        @Override
        protected TreeSet<K> initialValue() {
            return new TreeSet<>();
        }
    };

    private void rethrowLambdaException(RuntimeException e) throws InterruptedException {
        if ((e.getCause() != null) && (e.getCause() instanceof InterruptedException))
            throw ((InterruptedException) e.getCause());
        else if (e instanceof KeyLockManagerInterruptedException)
            throw new InterruptedException();
        else
            throw e;
    }

    protected abstract V instantiateValue(K key);

    private V provideAndLock(K key) throws InterruptedException {
        Lockable[] lockedValue = new Lockable[1];

        try {
            return lockManager.executeLocked(key, () -> {
                V value;

                if (map.containsKey(key))
                    value = map.get(key);
                else {
                    value = instantiateValue(key);
                    map.put(key, value);
                }

                // lock on chunk's lock object to protect it from being deleted after exiting from locked lambda,
                // but before locking on lock object, which will cause exception in reader thread
                lockedValue[0] = value;
                try {
                    value.getLock().lockInterruptibly();
                    Lockable.lockCount.incrementAndGet();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return value;
            });
        } catch (RuntimeException e) {
            if ((lockedValue[0] != null) && (lockedValue[0].getLock().isHeldByCurrentThread())) {
                System.out.println("manually unlocked");
                lockedValue[0].getLock().unlock();
            }

            rethrowLambdaException(e);
            return null; // will never be reached, but compiler forces to return from catch statement
        }
    }

    public LinkedHashMap<K, V> lockEntries(Iterable<K> keys) throws InterruptedException {
        Set<K> lockSet = lockedKeys.get();
        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() > 0) throw new RuntimeException("lock set has to be empty before locking!");

        LinkedHashMap<K, V> rslt = new LinkedHashMap<>();

        for (K key : keys) {
            lockSet.add(key);

            rslt.put(key, provideAndLock(key));
        }

        return rslt;
    }

    public V lockKey(K key) throws InterruptedException {
        Set<K> lockSet = lockedKeys.get();
        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() > 0) throw new RuntimeException("lock set has to be empty before locking!");

        lockSet.add(key);
        return provideAndLock(key);
    }

    public void unlock() {
        Set<K> lockSet = lockedKeys.get();

        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() == 0) throw new RuntimeException("lock can't be empty before unlocking!");

        for (K key : lockSet) {
            // unlock chunk to provide access to another readers and to allow this chunk to be removed.
            // chunk guaranteed to exists, because removeChunk() methods locks on removing chunk lock object
            if (map.containsKey(key)) {
                // TODO: 19.04.2016 describe why lock might be unlocked (interrupted exception during provide and lock)
//                if (map.get(key).getLock().isHeldByCurrentThread())
                    map.get(key).getLock().unlock();
                    Lockable.unlockCount.incrementAndGet();
//                else
//                    System.out.println(6543);
            } else
                System.out.println("123456789");
        }

        lockedKeys.remove();
    }

    public void put(K key, V value) {
        lockManager.executeLocked(key, () -> {
            map.put(key, value);
        });
    }

    public V getNonLocked(K key) {
        return map.get(key);
    }

    public boolean removeIf(K key, Function<V, Boolean> condition) throws InterruptedException {
        try {
            return lockManager.executeLocked(key, () -> {
                if (!map.containsKey(key))
                    return false;

                try {
                    V value = provideAndLock(key);
                    try {
                        boolean isRemove = true;
                        if (condition != null)
                            isRemove = condition.apply(value);

                        if (isRemove)
                            map.remove(key, value);

                        lockedKeys.get().remove(key);

                        return isRemove;
                    } finally {
                        value.getLock().unlock();
                        Lockable.unlockCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

//                return true;
            });
        } catch (RuntimeException e) {
            rethrowLambdaException(e);
            return false; // will never be reached, but compiler forces to return from catch statement
        }
    }

    public boolean remove(K key) throws InterruptedException {
        return removeIf(key, null);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
