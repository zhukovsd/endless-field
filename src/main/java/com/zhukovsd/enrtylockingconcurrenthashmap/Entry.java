package com.zhukovsd.enrtylockingconcurrenthashmap;

/**
 * Created by ZhukovSD on 24.04.2016.
 */
public class Entry<K, V> {
    private K key;
    private V value;

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
