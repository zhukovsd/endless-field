package com.zhukovsd.endlessfield.field;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public class ChunkIdGenerator {
    public static int idFactor = 40000;

    public static int generateID(ChunkSize chunkSize, CellPosition position) {
//        return 0;
        return (position.row / chunkSize.rowCount) * idFactor + (position.column / chunkSize.columnCount);
    }

    static CellPosition chunkOrigin(ChunkSize chunkSize, int chunkId) {
        return new CellPosition(
                (chunkId / idFactor) * chunkSize.rowCount,
                (chunkId % idFactor) * chunkSize.columnCount
        );
    }
}
