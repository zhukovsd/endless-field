package com.zhukovsd.endlessfield.field;

import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.endlessfield.fielddatasource.StoreChunkTask;
import com.zhukovsd.endlessfield.fielddatasource.UpdateCellTask;
import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;

import java.util.*;
import java.util.concurrent.*;

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
    // TODO: 23.03.2016 consider optimal thread pool size, queue class, queue size
    public ExecutorService chunkStoreExec = new ThreadPoolExecutor(10, 10,
            0L,TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());//Executors.newFixedThreadPool(10);

    // TODO: 29.03.2016 proper shutdown
    public ExecutorService cellUpdateExec = new ThreadPoolExecutor(10, 10,
            0L,TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());//Executors.newFixedThreadPool(10);

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

    public EndlessFieldChunk<T> provideAndLockChunk(Integer chunkId) {
        // lock on chunkId to prevent simultaneous creating / removing of chunk with same id by different threads.
        // if one thread entered this lambda, another thread, requesting chunk with same id will have to wait.
        return lockManager.executeLocked(chunkId, () -> {
            EndlessFieldChunk<T> chunk = null;

            try {
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

                    chunkStoreExec.submit(new StoreChunkTask<T>(dataSource, chunk, chunkId));
                }

                // lock on chunk's lock object to protect it from being deleted after exiting from locked lambda,
                // but before locking on lock object, which will cause exception in reader thread
                chunk.lock.lock();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return chunk;
        });
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

    // It is possible that some of entries to update belongs to currently storing, or queued to store chunk.
    // In that case update actions will have no effect and actual cell stored will be stored with entire chunk.
    public void updateEntries(Map<CellPosition, T> entries) {
        // get chunk ids for updating cells
        Set<Integer> chunkIds = new HashSet<>();
        for (Map.Entry<CellPosition, T> entry : entries.entrySet()) {
            chunkIds.add(ChunkIdGenerator.generateID(chunkSize, entry.getKey()));
        }

        // check is all chunk which owns updating cells are locked
        for (Integer chunkId : chunkIds) {
            if (!lockedChunkIds.get().contains(chunkId)) {
                // TODO: 25.03.2016 provide proper exception type
                throw new RuntimeException("chunk for updating cell is not locked!");
            }
        }

        // increment update task counts used to prevent chunk removing before all its tasks are finished
        for (Integer chunkId : chunkIds) chunkMap.get(chunkId).updateTaskCount.incrementAndGet();

        cellUpdateExec.submit(new UpdateCellTask<T>(this, dataSource, entries, chunkIds));
    }

    public CellEntry<T> getEntry(CellPosition position) {
        return new CellEntry<>(position, getCell(position));
    }

    public Iterable<T> getCells(Iterable<CellPosition> positions) {
        ArrayList<T> result = new ArrayList<>();
        for (CellPosition position : positions) result.add(getCell(position));

        return result;
    }

    public LinkedHashMap<CellPosition, T> getEntries(Iterable<CellPosition> positions) {
        LinkedHashMap<CellPosition, T> result = new LinkedHashMap<>();
        for (CellPosition position : positions) result.put(position, getCell(position));

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

    public void lockChunks(Iterable<CellPosition> positions) {
        Set<Integer> lockSet = lockedChunkIds.get();
        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() > 0) throw new RuntimeException("lock set has to be empty before locking!");

        for (CellPosition position : positions) lockSet.add(ChunkIdGenerator.generateID(chunkSize, position));

        // lock chunks to lock access by another readers and forbid it's removing
        for (Integer id : lockSet) provideAndLockChunk(id);
    }

    public void unlockChunks() {
        Set<Integer> lockSet = lockedChunkIds.get();

        // TODO: 25.03.2016 provide proper exception type
        if (lockSet.size() == 0) throw new RuntimeException("lock can't be empty before unlocking!");

        for (Integer chunkId : lockSet) {
            // unlock chunk to provide access to another readers and to allow this chunk to be removed.
            // chunk guaranteed to exists, because removeChunk() methods locks on removing chunk lock object
            if (chunkMap.containsKey(chunkId))
                chunkMap.get(chunkId).lock.unlock();
            else
                System.out.println("123456789");
        }

        lockedChunkIds.remove();
    }

    public void removeChunk(Integer chunkId) {
        // lock on chunkId to prevent access to this chunk in provideAndLockChunk() during its deletion.
        // otherwise, it is possible that reader thread will enter provideAndLockChunk(), chunk will be provided,
        // but before reader locks chunk's lock object, removing thread will be resumed and chunk will be removed
        // by the time reader thread will request it's cells
        lockManager.executeLocked(chunkId, () -> {
            try {
                // lock on chunk.lock to prevent its removing while being locked by another thread
                EndlessFieldChunk<T> chunk = provideAndLockChunk(chunkId);
                try {
                    chunkMap.remove(chunkId);
                } finally {
                    chunk.lock.unlock();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
