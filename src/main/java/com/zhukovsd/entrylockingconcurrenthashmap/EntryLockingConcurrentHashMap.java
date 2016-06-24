/*
 * Copyright 2016 Zhukov Sergei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhukovsd.entrylockingconcurrenthashmap;

import com.google.common.util.concurrent.Striped;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by ZhukovSD on 23.04.2016.
 */
public class EntryLockingConcurrentHashMap<K, V> {
    private ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    public Striped<Lock> striped;

    private ThreadLocal<TreeSet<K>> lockedKeys = new ThreadLocal<TreeSet<K>>() {
        @Override
        protected TreeSet<K> initialValue() {
            return new TreeSet<>();
        }
    };

    public EntryLockingConcurrentHashMap(int stripes) {
        striped = Striped.lock(stripes);
    }

    // TODO: 24.04.2016 return boolean
    private V provideAndLock(K key, Function<K, V> instantiator) throws InterruptedException {
        V value = null;

        // lock first to prevent race conditions on instantiating new entry
        Lock lock = striped.get(key);
        lock.lockInterruptibly();

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

    public boolean lockEntry(K key, Function<K, V> instantiator) throws InterruptedException {
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

    public boolean lockEntry(K key) throws InterruptedException {
        return lockEntry(key, null);
    }


//    public boolean lockEntries(Iterable<K> keys, Function<K, V> instaniator) throws InterruptedException {
//        Set<K> lockSet = lockedKeys.get();
//        // TODO: 25.03.2016 provide proper exception type
//        if (lockSet.size() > 0) throw new RuntimeException("striped set has to be empty before locking!");
//
//        boolean result = true;
//        for (K key : keys) {
//            result = (provideAndLock(key, instaniator) != null);
//
//            // add to set only on successful lock
//            if (result)
//                lockSet.add(key);
//            if (!result)
//                break;
//        }
//
//        // if we unable to lock all requested entries, unlock already locked ones
//        if (!result)
//            unlock();
//
//        return result;
//    }

    public boolean lockEntries(
            Collection<K> keys, Function<Collection<K>, Set<K>> relatedKeysFunction,
            BiFunction<K, Boolean, V> instantiator
    ) throws InterruptedException {
        Set<K> lockSet = lockedKeys.get();
        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() > 0) throw new RuntimeException("striped set has to be empty before locking!");

        boolean result = true;

        Set<K> relatedKeys;
        if (relatedKeysFunction != null) {
            relatedKeys = relatedKeysFunction.apply(keys);
        } else {
            relatedKeys = Collections.emptySet();
        }

        Iterable<K> keySet;
        if (relatedKeys.size() != 0) {
            HashSet<K> s = new HashSet<>(keys.size() + relatedKeys.size());
            s.addAll(keys);
            s.addAll(relatedKeys);

            keySet = s;
        } else {
            keySet = keys;
        }

        Iterable<Lock> locks = striped.bulkGet(keySet);
        for (Lock lock : locks) {
            lock.lockInterruptibly();
        }

        try {
            // provide
            for (K key : keySet) {
                boolean isRelated = relatedKeys.contains(key);

                V value = provide(key, isRelated, instantiator);

                if (!isRelated) {
                    if (value != null) {
                        // don't add related keys to lockSet, since related keys will be unlocked
                        lockSet.add(key);
                    } else {
                        result = false;
                        break;
                    }
                }
            }

            // if we unable to lock all requested entries, unlock all and clear lockSet
            if (!result) {
                for (Lock lock : locks) {
                    lock.unlock();
                }

                lockedKeys.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Iterable<Lock> relatedKeysLocks = striped.bulkGet(relatedKeys);

                for (Lock relatedKeyLock : relatedKeysLocks) {
                    relatedKeyLock.unlock();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public V provide(K key, Boolean isRelated, BiFunction<K, Boolean, V> instantiator) {
        V value = null;

        if (map.containsKey(key)) {
            value = map.get(key);
        }
        else {
            if (instantiator != null) {
                value = instantiator.apply(key, isRelated);

                if (value != null) {
                    map.put(key, value);
                }
            }
        }

        return value;
    }

    public boolean lockEntries(Collection<K> keys) throws InterruptedException {
        return lockEntries(keys, null, null);
    }

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
            } else
                System.out.println("123456789");
        }

        lockedKeys.remove();
    }

    public V getValue(K key) {
        if (!(lockedKeys.get().contains(key)))
            // TODO: 25.03.2016 provide proper exception type
            throw new RuntimeException("value for requested key has to be locked!");

        return map.get(key);
    }

    public LinkedHashMap<K, V> getEntries(Iterable<K> keys) {
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (K key : keys) result.put(key, getValue(key));

        return result;
    }

    public V getValueNonLocked(K key) {
        return map.get(key);
    }

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

    public boolean remove(K key) throws InterruptedException {
        return removeIf(key, null);
    }

    public Set<Map.Entry<K, V>> getEntriesNonLocked() {
        return map.entrySet();
    }

    public int size() {
        return map.size();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
