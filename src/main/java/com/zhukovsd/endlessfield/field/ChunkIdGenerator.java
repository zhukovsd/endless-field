package com.zhukovsd.endlessfield.field;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public class ChunkIdGenerator {
    static int idFactor = 40000;

    static int generateID(ChunkSize chunkSize, CellPosition position) {
//        return 0;
        return (position.row / chunkSize.rowCount) * idFactor + (position.column / chunkSize.columnCount);
    }
}
