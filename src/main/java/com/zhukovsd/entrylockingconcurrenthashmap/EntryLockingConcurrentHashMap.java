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
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by ZhukovSD on 23.04.2016.
 */
public class EntryLockingConcurrentHashMap<K, V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private final Striped<Lock> striped;
    private final Supplier<V> nullSupplier;
    private Class<?> nullClass;

    // TODO: 04.07.2016 switch to hash set?
    private ThreadLocal<TreeSet<K>> lockedKeys = new ThreadLocal<TreeSet<K>>() {
        @Override
        protected TreeSet<K> initialValue() {
            return new TreeSet<>();
        }
    };

    private ThreadLocal<HashSet<K>> reprovideKeys = new ThreadLocal<HashSet<K>>() {
        @Override
        protected HashSet<K> initialValue() {
            return new HashSet<>();
        }
    };

    public EntryLockingConcurrentHashMap(int stripes, Supplier<V> nullSupplier) {
        striped = Striped.lock(stripes);
        this.nullSupplier = nullSupplier;

        if (nullSupplier != null)
            nullClass = nullSupplier.get().getClass();
        else
            nullClass = null;
    }

    public EntryLockingConcurrentHashMap(int stripes) {
        this(stripes, null);
    }

    private V provide(K key, InstantiationData<K> data, ValueInstantiator<K, V> instantiator) {
        V value = null;

        // check if map already contains given and and it's value is not a null mock instance
        if (map.containsKey(key) && ((nullClass != null) && !(nullClass.isInstance(map.get(key))) || (nullClass == null))) {
            value = map.get(key);
        }
        else {
            // call instantiator
            if (instantiator != null) {
                InstantiationResult<V> result = instantiator.apply(key, data);

                if (result.type == InstantiationResultType.PROVIDED) {
                    // if value is provided, put it into the map and return as a result
                    map.put(key, result.value);
                    value = result.value;
                } else if (result.type == InstantiationResultType.NULL) {
                    // if value is null, put null mock instance into the map
                    map.put(key, nullSupplier.get());
                } else if (result.type == InstantiationResultType.NEED_RELATED_VALUE){
                    // if value was not instantiated due to lack of related values, postpone instantiation until second run
                    reprovideKeys.get().add(key);
                }
            }
        }

        return value;
    }

    public boolean lockEntries(
            Collection<K> keys, ValueInstantiator<K, V> instantiator, Function<K, Set<K>> relatedKeysFunction
    ) throws InterruptedException {
        Set<K> lockSet = lockedKeys.get();
        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() > 0) throw new RuntimeException("striped set has to be empty before locking!");

        boolean result = true;

        // determine related keys for all given keys, if related keys function is null, related set is empty
        Set<K> relatedKeys;
        if (relatedKeysFunction != null) {
            relatedKeys = new HashSet<>();
            for (K key : keys) {
                relatedKeys.addAll(relatedKeysFunction.apply(key));
            }
        } else {
            relatedKeys = Collections.emptySet();
        }

        // compose given keys and it's related keys into single keySet, which will be used for locking
        Iterable<K> keySet;
        if (relatedKeys.size() != 0) {
            HashSet<K> s = new HashSet<>(keys.size() + relatedKeys.size());
            s.addAll(keys);
            s.addAll(relatedKeys);

            keySet = s;
        } else {
            keySet = keys;
        }

        // lock on key set in order, provided by bulkGet (this order is always the same, so we don't have to sort keySet)
        Iterable<Lock> locks = striped.bulkGet(keySet);
        for (Lock lock : locks) {
            lock.lockInterruptibly();
        }

        try {
            HashSet<K> reprovideSet = reprovideKeys.get();

            // first run. provide values for all keys, postpone providing of values, which needs related values,
            // break from loop on NULL instantiation result
            for (K key : keySet) {
                boolean isRelated = !(keys.contains(key));

                V value = provide(
                        key, new InstantiationData<>(isRelated, map.containsKey(key), false, lockSet), instantiator
                );

                if (value != null) {
                    lockSet.add(key);
                } else if (!reprovideSet.contains(key) && !isRelated) {
                    // if instantiator for non-related key returned NULL result (instance can't be instantiated),
                    // break from loop and return false
                    result = false;
                    break;
                }
            }

            // second run. provide values only for postponed keys, break of loop on NULL instantiation result
            if (result) {
                for (K key : reprovideSet) {
                    boolean isRelated = !(keys.contains(key));

                    V value = provide(
                            key, new InstantiationData<>(isRelated, map.containsKey(key), true, lockSet), instantiator
                    );

                    if (value != null) {
                        lockSet.add(key);
                    } else if (!isRelated) {
                        // if instantiator for non-related key returned NULL result (instance can't be instantiated),
                        // break from loop and return false
                        result = false;
                        break;
                    }
                }
            }

            reprovideKeys.remove();

            // TODO: 25.06.2016 react to exceptions in instantiator

            // if we unable to lock all requested keys, unlock entries, which was already locked and clear lockSet
            if (!result) {
                for (Lock lock : locks) {
                    lock.unlock();
                }

                lockedKeys.remove();
            }
        } finally {
            // TODO: 04.07.2016 don't do this if result is false?

            // after all keys was locked, unlock related keys. don't unlock any keys, which belongs to "keys" set also
            relatedKeys.removeAll(keys);
            Iterable<Lock> relatedKeysLocks = striped.bulkGet(relatedKeys);

            for (Lock relatedKeyLock : relatedKeysLocks) {
                relatedKeyLock.unlock();
            }

            lockSet.removeAll(relatedKeys);
        }

        return result;
    }

    public boolean lockEntries(Collection<K> keys) throws InterruptedException {
        return lockEntries(keys, null, null);
    }

    public boolean lockEntry(
            K key, Function<K, Set<K>> relatedKeysFunction, ValueInstantiator<K, V> instantiator
    ) throws InterruptedException {
        return lockEntries(Collections.singleton(key), instantiator, relatedKeysFunction);
    }

    public boolean lockEntry(K key) throws InterruptedException {
        return lockEntry(key, null, null);
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

    public boolean removeIf(K key, Predicate<V> condition) throws InterruptedException {
        Lock lock = striped.get(key);
        lock.lockInterruptibly();
        try {
            if (!map.containsKey(key))
                return false;
            else {
                V value = map.get(key);

                boolean isRemove = true;
                // if value is "null" - always remove, otherwise evaluate condition
                if (!((nullClass != null) && (nullClass.isInstance(value))) && (condition != null))
                    isRemove = condition.test(value);

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
