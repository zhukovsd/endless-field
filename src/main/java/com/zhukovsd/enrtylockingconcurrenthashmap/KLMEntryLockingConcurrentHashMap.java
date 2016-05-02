package com.zhukovsd.enrtylockingconcurrenthashmap;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;
import de.jkeylockmanager.manager.exception.KeyLockManagerInterruptedException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
public class KLMEntryLockingConcurrentHashMap<K, V extends Lockable> implements EntryLockingConcurrentHashMap<K, V> {
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

    private V provideAndLock(K key, Function<K, V> instantiator) throws InterruptedException {
        Lockable[] lockedValue = new Lockable[1];

        try {
            return lockManager.executeLocked(key, () -> {
                V value = null;

                if (map.containsKey(key))
                    value = map.get(key);
                else {
                    if (instantiator != null) {
                        value = instantiator.apply(key);

                        if (value != null)
                            map.put(key, value);
                    }
                }

                // striped on chunk's striped object to protect it from being deleted after exiting from locked lambda,
                // but before locking on striped object, which will cause exception in reader thread
                lockedValue[0] = value;
                if (value != null) {
                    try {
                        value.getLock().lockInterruptibly();
                        Lockable.lockCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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

    @Override
    public boolean lockEntries(Iterable<K> keys, Function<K, V> instaniator) throws InterruptedException {
//        Set<K> lockSet = lockedKeys.get();
//        // TODO: 25.03.2016 provide proper exception type
//        if (lockSet.size() > 0) throw new RuntimeException("striped set has to be empty before locking!");
//
//        boolean rslt = true;
//        for (K key : keys) {
//            lockSet.add(key);
//            rslt = (provideAndLock(key, instaniator) == null);
//
//            if (!rslt)
//                break;
//        }
//
//        // if we unable to lock all requested entries, unlock already locked ones
//        if (!rslt)
//            unlock();

        return false;
    }

    @Override
    public boolean lockEntries(Iterable<K> keys) throws InterruptedException {
        return lockEntries(keys, null);
    }

    @Override
    public boolean lockKey(K key, Function<K, V> instantiator) throws InterruptedException {
        Set<K> lockSet = lockedKeys.get();
        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() > 0) throw new RuntimeException("striped set has to be empty before locking!");

        V value = provideAndLock(key, instantiator);
        if (value != null) {
            lockSet.add(key);
            return true;
        } else
            return false;
    }

    @Override
    public boolean lockKey(K key) throws InterruptedException {
        return lockKey(key, null);
    }

    @Override
    public void unlock() {
        Set<K> lockSet = lockedKeys.get();

        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() == 0) throw new RuntimeException("striped can't be empty before unlocking!");

        for (K key : lockSet) {
            // unlock chunk to provide access to another readers and to allow this chunk to be removed.
            // chunk guaranteed to exists, because removeChunk() methods locks on removing chunk striped object
            if (map.containsKey(key)) {
                // TODO: 19.04.2016 describe why striped might be unlocked (interrupted exception during provide and striped)
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

    @Override
    public V getValue(K key) {
        if (!(lockedKeys.get().contains(key)))
            // TODO: 25.03.2016 provide proper exception type
            throw new RuntimeException("value for requested key has to be locked!");

        return map.get(key);
    }

    @Override
    public LinkedHashMap<K, V> getEntries(Iterable<K> keys) {
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (K key : keys) result.put(key, getValue(key));

        return result;
    }

    @Override
    public V getValueNonLocked(K key) {
        return map.get(key);
    }

    @Override
    public void put(K key, V value) {
        lockManager.executeLocked(key, () -> {
            map.put(key, value);
        });
    }

    @Override
    public boolean removeIf(K key, Function<V, Boolean> condition) throws InterruptedException {
        try {
            return lockManager.executeLocked(key, () -> {
                if (!map.containsKey(key))
                    return false;

                try {
                    V value = provideAndLock(key, null);
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

    @Override
    public boolean remove(K key) throws InterruptedException {
        return removeIf(key, null);
    }

    @Override
    public Set<Map.Entry<K, V>> getEntriesNonLocked() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public int size() {
        return map.size();
    }
}
