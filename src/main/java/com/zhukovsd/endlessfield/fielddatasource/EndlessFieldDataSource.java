package com.zhukovsd.endlessfield.fielddatasource;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;

import java.util.Map;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public interface EndlessFieldDataSource<T extends EndlessFieldCell> {
    boolean containsChunk(Integer chunkId);
    EndlessFieldChunk<T> getChunk(Integer chunkId, ChunkSize chunkSize);

    void storeChunk(EndlessFieldChunk<T> chunk, int chunkId);
    void modifyEntries(Map<CellPosition, T> entries);
}
