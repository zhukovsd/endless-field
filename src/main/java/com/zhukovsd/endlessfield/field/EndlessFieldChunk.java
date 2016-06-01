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

package com.zhukovsd.endlessfield.field;

import com.zhukovsd.endlessfield.CellPosition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
