package com.zhukovsd.endlessfield.field;

import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.endlessfield.fielddatasource.StoreChunkTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ZhukovSD on 13.03.2016.
 */
public abstract class EndlessField<T extends EndlessFieldCell> {
    private ChunkSize chunkSize;
    private EndlessFieldDataSource<T> dataSource;
    private EndlessFieldCellFactory<T> cellFactory;
    // TODO: 21.03.2016 add field size constraints

    private ConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap = new ConcurrentHashMap<>();
    // TODO: 22.03.2016 proper shutdown
    // TODO: 23.03.2016 consider optimal thread pool size
    private ExecutorService chunkStoreExec = Executors.newFixedThreadPool(5);

    private ThreadLocal<EndlessFieldChunk<T>> lockedChunk = new ThreadLocal<>();

    public EndlessField(ChunkSize chunkSize, EndlessFieldDataSource<T> dataSource, EndlessFieldCellFactory<T> cellFactory) {
        this.chunkSize = chunkSize;
        this.dataSource = dataSource;
        this.cellFactory = cellFactory;
    }

    private EndlessFieldChunk<T> getChunk(Integer chunkId) {
        EndlessFieldChunk<T> chunk;

        // get already loaded chunk
        if (chunkMap.containsKey(chunkId))
            chunk = chunkMap.get(chunkId);
        // get stored, but not loaded chunk
        else if (dataSource.containsChunk(chunkId)) {
            chunk = dataSource.getChunk(chunkId, chunkSize);
            // TODO: 21.03.2016 check if chunk has correct size
            chunk.setStored(true);

            chunkMap.put(chunkId, chunk);
        // generate new chunk and store it
        } else {
            chunk = generateChunk(chunkId);
            chunkMap.put(chunkId, chunk);

//            dataSource.storeChunk(chunk, chunkId);
//            chunkStoreExec.submit(new StoreChunkTask<T>(dataSource, chunk, chunkId));
        }

        return chunk;
    }

    public T getCell(CellPosition position) {
        // TODO: 21.03.2016 check if cell position is correct (within bounds)
        Integer chunkId = ChunkIdGenerator.generateID(chunkSize, position);
        EndlessFieldChunk<T> chunk = getChunk(chunkId);

        return chunk.get(position);
    }

    public CellEntry<T> getEntry(CellPosition position) {
        return new CellEntry<>(position, getCell(position));
    }

    public Iterable<T> getCells(Iterable<CellPosition> positions) {
        // sorts ids in ascending order (0 -> n)
        TreeSet<Integer> chunkIds = new TreeSet<>();
        for (CellPosition position : positions) {
            chunkIds.add(ChunkIdGenerator.generateID(chunkSize, position));
        }

        // provide chunk presence
        for (Integer chunkId : chunkIds) getChunk(chunkId);

        ArrayList<T> result = new ArrayList<>();
        // lock exactly before reading
        for (Integer chunkId : chunkIds) chunkMap.get(chunkId).lock.lock();

        try {
            for (CellPosition position : positions) {
                chunkMap.get(ChunkIdGenerator.generateID(chunkSize, position)).get(position);
            }
        } finally {
            for (Integer chunkId : chunkIds) chunkMap.get(chunkId).lock.unlock();
        }

        return result;
    }

    private EndlessFieldChunk<T> generateChunk(int chunkId) {
        EndlessFieldChunk<T> chunk = new EndlessFieldChunk<>(chunkSize.cellCount());
        CellPosition chunkOrigin = ChunkIdGenerator.chunkOrigin(chunkSize, chunkId);

        for (int row = 0; row < chunkSize.rowCount; row++) {
            for (int column = 0; column < chunkSize.columnCount; column++) {
                chunk.put(new CellPosition(chunkOrigin.row + row, chunkOrigin.column + column), cellFactory.create());
            }
        }

        return chunk;
    }

    public Set<Map.Entry<Integer, EndlessFieldChunk<T>>> chunkSet() {
        return chunkMap.entrySet();
    }

    public EndlessFieldChunk<T> test(Integer chunkId) {
        return chunkMap.get(chunkId);
    }

    public void lockChunk(Integer chunkId) {
        EndlessFieldChunk<T> chunk = getChunk(chunkId);

        lockedChunk.set(chunk);
        chunk.lock.lock();

        System.out.println("chunk " + chunk + " locked by thread " + Thread.currentThread().getName());
    }

    public void unlockChunk() {
        EndlessFieldChunk<T> chunk = lockedChunk.get();

        chunk.lock.unlock();
        lockedChunk.remove();

        System.out.println("chunk " + chunk + " unlocked by thread " + Thread.currentThread().getName());
    }

    void commitCellsUpdate(Iterable<CellEntry<T>> cellEntries) {

    }
}
