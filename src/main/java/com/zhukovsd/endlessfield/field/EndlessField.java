package com.zhukovsd.endlessfield.field;

import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.endlessfield.fielddatasource.StoreChunkTask;
import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ZhukovSD on 13.03.2016.
 */
public abstract class EndlessField<T extends EndlessFieldCell> {
    public ChunkSize chunkSize;
    private EndlessFieldDataSource<T> dataSource;
    private EndlessFieldCellFactory<T> cellFactory;
    // TODO: 21.03.2016 add field size constraints

    // TODO: 25.03.2016 hide to private
    public ConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap = new ConcurrentHashMap<>();

    // TODO: 22.03.2016 proper shutdown
    // TODO: 23.03.2016 consider optimal thread pool size
    private ExecutorService chunkStoreExec = Executors.newFixedThreadPool(5);

    // TODO: 29.03.2016 proper shutdown
    private ExecutorService cellUpdateExec;

    // TODO: 25.03.2016 test deletion with KLM locking
    private final KeyLockManager lockManager = KeyLockManagers.newLock();

    private ThreadLocal<TreeSet<Integer>> lockedChunkIds = new ThreadLocal<TreeSet<Integer>>() {
        @Override
        protected TreeSet<Integer> initialValue() {
            return new TreeSet<>(); // TODO: 25.03.2016 provide explicit comparator
        }
    };

    public EndlessField(ChunkSize chunkSize, EndlessFieldDataSource<T> dataSource, EndlessFieldCellFactory<T> cellFactory) {
        this.chunkSize = chunkSize;
        this.dataSource = dataSource;
        this.cellFactory = cellFactory;
    }

    public EndlessFieldChunk<T> provideChunk(Integer chunkId) {
        EndlessFieldChunk<T> chunk = lockManager.executeLocked(chunkId, () -> {
            // get already loaded chunk
            if (chunkMap.containsKey(chunkId))
                return chunkMap.get(chunkId);
            // get stored, but not loaded chunk
            else if (dataSource.containsChunk(chunkId)) {
                EndlessFieldChunk<T> c = dataSource.getChunk(chunkId, chunkSize);
                // TODO: 21.03.2016 check if chunk has correct size
                c.setStored(true);
                chunkMap.put(chunkId, c);
                return c;
            // generate new chunk and store it
            } else {
                EndlessFieldChunk<T> c = generateChunk(chunkId);
                chunkMap.put(chunkId, c);

                dataSource.storeChunk(c, chunkId);
                chunkStoreExec.submit(new StoreChunkTask<T>(dataSource, c, chunkId));

                return c;
            }
        });

        return chunk;
    }

    public T getCell(CellPosition position) {
        if (!(lockedChunkIds.get().contains(ChunkIdGenerator.generateID(chunkSize, position)))) {
            // TODO: 25.03.2016 provide proper exception type
            throw new RuntimeException("chunk for requested position is not locked!");
        }

        // TODO: 21.03.2016 check if cell position is correct (within bounds)
        Integer chunkId = ChunkIdGenerator.generateID(chunkSize, position);
        EndlessFieldChunk<T> chunk = chunkMap.get(chunkId);

        return chunk.get(position);
    }

    public void updateCell(CellPosition position) {
        if (!(lockedChunkIds.get().contains(ChunkIdGenerator.generateID(chunkSize, position)))) {
            // TODO: 25.03.2016 provide proper exception type
            throw new RuntimeException("chunk for updating cell is not locked!");
        }


    }

    public CellEntry<T> getEntry(CellPosition position) {
        return new CellEntry<>(position, getCell(position));
    }

    public Iterable<T> getCells(Iterable<CellPosition> positions) {
        ArrayList<T> result = new ArrayList<>();
        for (CellPosition position : positions) result.add(getCell(position));

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

//    public Set<Map.Entry<Integer, EndlessFieldChunk<T>>> chunkSet() {
//        return chunkMap.entrySet();
//    }

    // TODO: 25.03.2016 lock chunk(s) by cell positions
    public void lockChunks(Iterable<CellPosition> positions) {
//        EndlessFieldChunk<T> chunk = provideChunk(chunkId);

        Set<Integer> lockSet = lockedChunkIds.get();
        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() > 0) throw new RuntimeException("lock set has to be empty before locking!");

        for (CellPosition position : positions) lockSet.add(ChunkIdGenerator.generateID(chunkSize, position));

        // TODO: 25.03.2016 test if 2 loops better than one
        for (Integer id : lockSet) provideChunk(id);
        for (Integer id : lockSet) chunkMap.get(id).lock.lock();

//        for (EndlessFieldChunk<T> chunk : lockSet)
//        chunk.lock.lock();
    }

    public void unlockChunks() {
        Set<Integer> lockSet = lockedChunkIds.get();

        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() == 0) throw new RuntimeException("lock can't be empty before unlocking!");

        // we assume that all chunks exists due to being provided by lockChunks() call
        for (Integer id : lockSet) chunkMap.get(id).lock.unlock();

        lockedChunkIds.remove();
    }

    void commitCellsUpdate(Iterable<CellEntry<T>> cellEntries) {

    }
}
