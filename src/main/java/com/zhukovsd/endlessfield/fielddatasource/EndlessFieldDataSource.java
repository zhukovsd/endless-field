package com.zhukovsd.endlessfield.fielddatasource;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.enrtylockingconcurrenthashmap.StripedEntryLockingConcurrentHashMap;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public interface EndlessFieldDataSource<T extends EndlessFieldCell> {
    boolean containsChunk(Integer chunkId);
    EndlessFieldChunk<T> getChunk(Integer chunkId, ChunkSize chunkSize);

    void storeChunk(StripedEntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap, int chunkId) throws InterruptedException;
    void modifyEntries(Map<CellPosition, T> entries);
}
