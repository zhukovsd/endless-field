package com.zhukovsd.endlessfield.field;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by ZhukovSD on 13.03.2016.
 */
public class EndlessFieldChunk<T extends EndlessFieldCell> {
    private ConcurrentHashMap<CellPosition, T> cellsMap;

    // TODO: 23.03.2016 set to true on read from data source
    private boolean isStored = false;

    public ReentrantLock lock = new ReentrantLock(); // TODO: 23.03.2016 remove public
    public ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    public StampedLock stampedLock = new StampedLock();

    public EndlessFieldChunk(int capacity) {
        cellsMap = new ConcurrentHashMap<>(capacity);
    }

    public T get(CellPosition key) {
        return cellsMap.get(key);
    }

    public T put(CellPosition key, T value) {
        return cellsMap.put(key, value);
    }

    public Set<Map.Entry<CellPosition, T>> entrySet() {
        return cellsMap.entrySet();
    }

    public synchronized boolean isStored() {
        return isStored;
    }

    public synchronized void setStored(boolean stored) {
        isStored = stored;
    }
}
