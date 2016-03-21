package com.zhukovsd.endlessfield.field;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhukovSD on 13.03.2016.
 */
public class EndlessFieldChunk<T extends EndlessFieldCell> {
    ConcurrentHashMap<CellPosition, T> cellsMap;

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
}
