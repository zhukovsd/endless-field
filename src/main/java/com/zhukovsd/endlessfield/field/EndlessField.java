package com.zhukovsd.endlessfield.field;

import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhukovSD on 13.03.2016.
 */
public abstract class EndlessField<T extends EndlessFieldCell> {
    private ChunkSize chunkSize;
    private EndlessFieldDataSource<T> dataSource;
    private EndlessFieldCellFactory<T> cellFactory;
    //// TODO: 21.03.2016 add field size constraints

    private ConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap = new ConcurrentHashMap<>();

    public EndlessField(ChunkSize chunkSize, EndlessFieldDataSource<T> dataSource, EndlessFieldCellFactory<T> cellFactory) {
        this.chunkSize = chunkSize;
        this.dataSource = dataSource;
        this.cellFactory = cellFactory;
    }

    public T getCell(CellPosition position) {
        // TODO: 21.03.2016 check if cell position is correct
        Integer chunkId = ChunkIdGenerator.generateID(chunkSize, position);
        EndlessFieldChunk<T> chunk;

        // get already loaded chunk
        if (chunkMap.containsKey(chunkId))
            chunk = chunkMap.get(chunkId);
        // get stored, but not loaded chunk
        else if (dataSource.containsChunk(chunkId)) {
            chunk = dataSource.getChunk(chunkId, chunkSize);
            // TODO: 21.03.2016 check if chunk has correct size

            chunkMap.put(chunkId, chunk);
        // create new chunk and store it
        } else {
            chunk = generateChunk();
            chunkMap.put(chunkId, chunk);

            // TODO: 21.03.2016 store chunk
            dataSource.storeChunk(chunk, chunkId);
        }

        return chunk.get(position);
    }

    public EndlessFieldChunk<T> generateChunk() {
        EndlessFieldChunk<T> chunk = new EndlessFieldChunk<>(chunkSize.cellCount());

        for (int row = 0; row < chunkSize.rowCount; row++) {
            for (int column = 0; column < chunkSize.columnCount; column++) {
                chunk.put(new CellPosition(row, column), cellFactory.create());
            }
        }

        return chunk;
    }
}
