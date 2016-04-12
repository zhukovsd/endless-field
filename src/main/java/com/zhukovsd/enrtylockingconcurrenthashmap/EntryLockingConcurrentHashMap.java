package com.zhukovsd.enrtylockingconcurrenthashmap;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
public abstract class EntryLockingConcurrentHashMap<K, V extends Lockable> {
    ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    KeyLockManager lockManager = KeyLockManagers.newLock();

    private ThreadLocal<TreeSet<K>> lockedKeys = new ThreadLocal<TreeSet<K>>() {
        @Override
        protected TreeSet<K> initialValue() {
            return new TreeSet<>();
        }
    };

    protected abstract V instantiateValue(K key);

    private V provideAndLock(K key) {
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
            try {
                value.lock.lockInterruptibly();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return value;
        });
    }

    public LinkedHashMap<K, V> lockEntries(Iterable<K> keys) {
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

    public V lockKey(K key) {
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
            if (map.containsKey(key))
                map.get(key).lock.unlock();
            else
                System.out.println("123456789");
        }

        lockedKeys.remove();
    }

    public void put(K key, V value) {
        lockManager.executeLocked(key, () -> {
            map.put(key, value);
        });
    }

    public boolean remove(K key) {
        return lockManager.executeLocked(key, () -> {
            if (!map.containsKey(key))
                return false;

            V value = provideAndLock(key);
            try {
                map.remove(key);
            } finally {
                value.lock.unlock();
            }

            return true;
        });
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
