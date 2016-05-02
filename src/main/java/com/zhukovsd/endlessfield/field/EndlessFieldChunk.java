package com.zhukovsd.endlessfield.field;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by ZhukovSD on 13.03.2016.
 */
public class EndlessFieldChunk<T extends EndlessFieldCell> {
    private ConcurrentHashMap<CellPosition, T> cellsMap;

    public EndlessFieldChunk(int capacity) {
        cellsMap = new ConcurrentHashMap<>(capacity);
    }

    // TODO: 23.03.2016 set to true on read from data source
    private boolean isStored = false;

    public synchronized boolean isStored() {
        return isStored;
    }

    public synchronized void setStored(boolean stored) {
        isStored = stored;
    }

    // TODO: 25.04.2016 remove debug field
    public AtomicInteger updateTaskCount = new AtomicInteger(0);

    // lock object, used to lock access to cells, isStored() and another additional fields may
    // be accessed w/o using this lock and have to provide synchronization by themselves
//    public ReentrantLock lock = new ReentrantLock(); // TODO: 23.03.2016 remove public
    // TODO: 04.04.2016 remove unused locks
//    public ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

//    public StampedLock stampedLock = new StampedLock();

    public T get(CellPosition key) {
        return cellsMap.get(key);
    }

    public T put(CellPosition key, T value) {
        return cellsMap.put(key, value);
    }

    public ConcurrentHashMap<CellPosition, T> cellsMap() {
        return cellsMap;
    }
}
