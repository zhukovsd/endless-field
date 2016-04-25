package com.zhukovsd.enrtylockingconcurrenthashmap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by ZhukovSD on 23.04.2016.
 */
public interface EntryLockingConcurrentHashMap<K, V> {
    boolean lockKey(K key, Function<K, V> instaniator) throws InterruptedException;

    boolean lockKey(K key) throws InterruptedException;

    boolean lockEntries(Iterable<K> keys, Function<K, V> instaniator) throws InterruptedException;

    boolean lockEntries(Iterable<K> keys) throws InterruptedException;

    void unlock();

    V getValue(K key);

    V getValueNonLocked(K key);

    LinkedHashMap<K, V> getEntries(Iterable<K> keys);

    void put(K key, V value);

    boolean removeIf(K key, Function<V, Boolean> condition) throws InterruptedException;

    boolean remove(K key) throws InterruptedException;

    Set<Map.Entry<K, V>> getEntriesNonLocked();

    int size();
}
