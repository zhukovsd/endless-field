package com.zhukovsd.enrtylockingconcurrenthashmap;

import com.google.common.util.concurrent.Striped;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Created by ZhukovSD on 23.04.2016.
 */
public class StripedEntryLockingConcurrentHashMap<K, V> implements EntryLockingConcurrentHashMap<K, V> {
    private ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    public Striped<Lock> striped;

    private ThreadLocal<TreeSet<K>> lockedKeys = new ThreadLocal<TreeSet<K>>() {
        @Override
        protected TreeSet<K> initialValue() {
            return new TreeSet<>();
        }
    };

    public StripedEntryLockingConcurrentHashMap(int stripes) {
        striped = Striped.lock(stripes);
    }

    // TODO: 24.04.2016 return boolean
    private V provideAndLock(K key, Function<K, V> instantiator) throws InterruptedException {
        V value = null;

        // lock first to prevent race conditions on instantiating new entry
        Lock lock = striped.get(key);
        lock.lockInterruptibly();
        Lockable.lockCount.incrementAndGet();

        try {
            if (map.containsKey(key)) {
                value = map.get(key);
            }
            else {
                if (instantiator != null) {
                    value = instantiator.apply(key);

                    if (value != null) {
                        map.put(key, value);
                    }
                }
            }
        } finally {
            if (value == null)
                lock.unlock();
        }

        return value;
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
    public boolean lockEntries(Iterable<K> keys, Function<K, V> instaniator) throws InterruptedException {
        Set<K> lockSet = lockedKeys.get();
        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() > 0) throw new RuntimeException("striped set has to be empty before locking!");

        boolean result = true;
        for (K key : keys) {
            result = (provideAndLock(key, instaniator) != null);

            // add to set only on successful lock
            if (result)
                lockSet.add(key);
            if (!result)
                break;
        }

        // if we unable to lock all requested entries, unlock already locked ones
        if (!result)
            unlock();

        return result;
    }

    @Override
    public boolean lockEntries(Iterable<K> keys) throws InterruptedException {
        return lockEntries(keys, null);
    }

    @Override
    public void unlock() {
        Set<K> lockSet = lockedKeys.get();

        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() == 0) throw new RuntimeException("lock set can't be empty before unlocking!");

        for (K key : lockSet) {
            // unlock chunk to provide access to another readers and to allow this chunk to be removed.
            // chunk guaranteed to exist
            if (map.containsKey(key)) {
                // TODO: 19.04.2016 describe why striped might be unlocked (interrupted exception during provide and striped)
                striped.get(key).unlock();
                Lockable.unlockCount.incrementAndGet();
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

    }

    @Override
    public boolean removeIf(K key, Function<V, Boolean> condition) throws InterruptedException {
//        V value = provideAndLock(key, null);
        Lock lock = null;

        lock = striped.get(key);
        lock.lockInterruptibly();
        try {
            if (!map.containsKey(key))
                return false;
            else {
                V value = map.get(key);

                boolean isRemove = true;
                if (condition != null)
                    isRemove = condition.apply(value);

                if (isRemove)
                    map.remove(key, value);

                lockedKeys.get().remove(key);

                return isRemove;
            }
        } finally {
            lock.unlock();
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
