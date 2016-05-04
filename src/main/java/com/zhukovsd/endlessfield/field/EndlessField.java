package com.zhukovsd.endlessfield.field;

import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.endlessfield.fielddatasource.StoreChunkTask;
import com.zhukovsd.endlessfield.fielddatasource.UpdateCellTask;
import com.zhukovsd.enrtylockingconcurrenthashmap.Entry;
import com.zhukovsd.enrtylockingconcurrenthashmap.StripedEntryLockingConcurrentHashMap;

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
//    public ConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap = new ConcurrentHashMap<>();
    private StripedEntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap;

    // TODO: 22.03.2016 proper shutdown
    // TODO: 23.03.2016 consider optimal thread pool size, queue class, queue size
    public ExecutorService chunkStoreExec = new ThreadPoolExecutor(10, 10,
            0L,TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());//Executors.newFixedThreadPool(10);

    // TODO: 29.03.2016 proper shutdown
    public ExecutorService cellUpdateExec = new ThreadPoolExecutor(10, 10,
            0L,TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());//Executors.newFixedThreadPool(10);

//    private final KeyLockManager lockManager = KeyLockManagers.newLock();

//    private ThreadLocal<TreeSet<Integer>> lockedChunkIds = new ThreadLocal<TreeSet<Integer>>() {
//        @Override
//        protected TreeSet<Integer> initialValue() {
//            return new TreeSet<>(); // TODO: 25.03.2016 provide explicit comparator
//        }
//    };

    public EndlessField(int stripes, ChunkSize chunkSize, EndlessFieldDataSource<T> dataSource, EndlessFieldCellFactory<T> cellFactory) {
        chunkMap = new StripedEntryLockingConcurrentHashMap<>(stripes);
        this.chunkSize = chunkSize;
        this.dataSource = dataSource;
        this.cellFactory = cellFactory;
    }

//    public EndlessFieldChunk<T> provideAndLockChunk(Integer chunkId) {
//        // lock on chunkId to prevent simultaneous creating / removing of chunk with same id by different threads.
//        // if one thread entered this lambda, another thread, requesting chunk with same id will have to wait.
//        return lockManager.executeLocked(chunkId, () -> {
//            EndlessFieldChunk<T> chunk = null;
//
//            try {
//                // get already loaded chunk
//                if (chunkMap.containsKey(chunkId))
//                    chunk = chunkMap.get(chunkId);
//                // get stored, but not loaded chunk
//                else if (dataSource.containsChunk(chunkId)) {
//                    chunk = dataSource.getChunk(chunkId, chunkSize);
//                    // TODO: 21.03.2016 check if chunk has correct size
//                    chunk.setStored(true);
//                    chunkMap.put(chunkId, chunk);
//                // generate new chunk and store it
//                } else {
//                    chunk = generateChunk(chunkId);
//                    chunkMap.put(chunkId, chunk);
//
//                    chunkStoreExec.submit(new StoreChunkTask<>(dataSource, chunk, chunkId));
//                }
//
//                // lock on chunk's lock object to protect it from being deleted after exiting from locked lambda,
//                // but before locking on lock object, which will cause exception in reader thread
//                chunk.lock.lock();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            return chunk;
//        });
//    }

    // TODO: 25.04.2016 this method might throw exception
    EndlessFieldChunk<T> instantiateChunk(Integer chunkId) {
        EndlessFieldChunk<T> chunk;

        // get stored, but not loaded chunk
        if (dataSource.containsChunk(chunkId)) {
            chunk = dataSource.getChunk(chunkId, chunkSize);
            // TODO: 21.03.2016 check if chunk has correct size
            chunk.setStored(true);
            chunkMap.put(chunkId, chunk);
        // generate new chunk and store it
        } else {
            chunk = generateChunk(chunkId);
            chunkMap.put(chunkId, chunk);

            chunkStoreExec.submit(new StoreChunkTask<>(dataSource, chunkMap, chunkId));
        }

        return chunk;
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

    public boolean lockChunksByIds(Iterable<Integer> chunkIds) throws InterruptedException {
        return chunkMap.lockEntries(chunkIds, this::instantiateChunk);
    }

    public boolean lockChunksByPositions(Iterable<CellPosition> positions) throws InterruptedException {
        Set<Integer> chunkIds = new HashSet<>();

        for (CellPosition position : positions) chunkIds.add(ChunkIdGenerator.generateID(chunkSize, position));

        return lockChunksByIds(chunkIds);
    }

    public void unlockChunks() {
        chunkMap.unlock();
    }

    public T getCell(CellPosition position) {
        // TODO: 21.03.2016 check if cell position is correct (within bounds) ?
        Integer chunkId = ChunkIdGenerator.generateID(chunkSize, position);
        EndlessFieldChunk<T> chunk = chunkMap.getValue(chunkId);

        return chunk.get(position);
    }

    public Iterable<T> getCells(Iterable<CellPosition> positions) {
        ArrayList<T> result = new ArrayList<>();
        for (CellPosition position : positions) result.add(getCell(position));

        return result;
    }

    public Entry<CellPosition, T> getEntry(CellPosition position) {
        return new Entry<>(position, getCell(position));
    }

    public LinkedHashMap<CellPosition, T> getEntries(Iterable<CellPosition> positions) {
        LinkedHashMap<CellPosition, T> result = new LinkedHashMap<>();
        for (CellPosition position : positions) result.put(position, getCell(position));

        return result;
    }

    // TODO: 04.05.2016 linked hash map as a result makes to sense here, because chunk stores as hashmap
    public LinkedHashMap<CellPosition, T> getEntriesByChunkIds(Iterable<Integer> chunkIds) {
        LinkedHashMap<CellPosition, T> result = new LinkedHashMap<>();
        for (Integer chunkId : chunkIds) {
            EndlessFieldChunk<T> chunk = chunkMap.getValue(chunkId);
            result.putAll(chunk.cellsMap());
        }

        return result;
    }

    // It is possible that some of entries to update belongs to currently storing, or queued to store chunk.
    // In that case update actions will have no effect (we update existing db record here),
    // in this case cell will be stored with entire chunk during StoreChunkTask execution
    public void updateEntries(Map<CellPosition, T> entries) {
        // get chunk ids for updating cells
        Set<Integer> chunkIds = new HashSet<>();
        for (Map.Entry<CellPosition, T> entry : entries.entrySet()) {
            chunkIds.add(ChunkIdGenerator.generateID(chunkSize, entry.getKey()));
        }

        // increment update task counts used to prevent chunk removing before all its tasks are finished
        for (Integer chunkId : chunkIds) chunkMap.getValue(chunkId).updateTaskCount.incrementAndGet();

        cellUpdateExec.submit(new UpdateCellTask<>(dataSource, chunkMap, entries, chunkIds));
    }

    public void removeChunk(Integer chunkId) throws InterruptedException {
        chunkMap.remove(chunkId);
    }

    public int size() {
        return chunkMap.size();
    }
}
