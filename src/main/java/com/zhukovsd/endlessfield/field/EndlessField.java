/*
 * Copyright 2016 Zhukov Sergei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhukovsd.endlessfield.field;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkIdGenerator;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.EndlessFieldSizeConstraints;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.endlessfield.fielddatasource.StoreChunkTask;
import com.zhukovsd.endlessfield.fielddatasource.UpdateCellTask;
import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
import com.zhukovsd.entrylockingconcurrenthashmap.InstantiationData;
import com.zhukovsd.entrylockingconcurrenthashmap.InstantiationResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhukovSD on 13.03.2016.
 */
public abstract class EndlessField<T extends EndlessFieldCell> {
    public final ChunkSize chunkSize;
    public final EndlessFieldSizeConstraints sizeConstraints;

    private final EndlessFieldDataSource<T> dataSource;
    private final EndlessFieldCellFactory<T> cellFactory;
    private final EndlessFieldChunkFactory<T> chunkFactory;
    public final EndlessFieldActionInvoker actionInvoker;

    private EntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<T>> chunkMap;

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

    public EndlessField(int stripes, ChunkSize chunkSize, EndlessFieldSizeConstraints sizeConstraints, EndlessFieldDataSource<T> dataSource, EndlessFieldCellFactory<T> cellFactory) {
        this.chunkSize = chunkSize;
        this.sizeConstraints = sizeConstraints;
        this.dataSource = dataSource;
        this.cellFactory = cellFactory;

        chunkMap = new EntryLockingConcurrentHashMap<>(stripes, NullEndlessFieldChunk::new);

        chunkFactory = createChunkFactory();
        actionInvoker = createActionInvoker();

    }

    protected abstract EndlessFieldActionInvoker createActionInvoker();
    protected abstract EndlessFieldChunkFactory<T> createChunkFactory();

    public static EndlessField instantiate(
            String className, int stripes, ChunkSize chunkSize, EndlessFieldDataSource dataSource,
            EndlessFieldCellFactory cellFactory
    ) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> fieldType = Class.forName(className);

        Constructor<?> constructor = fieldType.getConstructor(
                int.class, ChunkSize.class, EndlessFieldDataSource.class, EndlessFieldCellFactory.class
        );

        return (EndlessField) constructor.newInstance(stripes, chunkSize, dataSource, cellFactory);
    }

    protected Set<Integer> relatedChunks(Integer chunkId) {
        return Collections.emptySet();
    }

    // TODO: 25.04.2016 this method might throw exception
    private InstantiationResult<EndlessFieldChunk<T>> instantiateChunk(
            Integer chunkId, InstantiationData<Integer> data
    ) {
        EndlessFieldChunk<T> chunk;
        InstantiationResult<EndlessFieldChunk<T>> result;

        System.out.format(
                "chunk id = %s, isRelated = %s, isNull = %s, set = %s, related ids = %s\n",
                chunkId, data.isRelated, data.isNull, data.lockedKeys.toString(), relatedChunks(chunkId)
        );

        // get stored, but not loaded chunk
        if (!data.isNull && dataSource.containsChunk(chunkId)) {
            chunk = dataSource.getChunk(chunkId, chunkSize);
            // TODO: 21.03.2016 check if chunk has correct size
            chunk.setStored(true);

            result = InstantiationResult.provided(chunk);
        } else if (!data.isRelated) {
            // generate new chunk and store it
            Set<Integer> relatedChunks = relatedChunks(chunkId);

            if (relatedChunks.contains(chunkId)) {
                throw new RuntimeException("related chunks can't contain current chunk id");
            }

            if (data.lockedKeys.containsAll(relatedChunks) || data.isReproviding) {
//                chunk = generateChunk(chunkId);
                chunk = chunkFactory.generateChunk(chunkId, data.lockedKeys);

                chunkStoreExec.submit(new StoreChunkTask<>(dataSource, chunkMap, chunkId, chunk));

                result = InstantiationResult.provided(chunk);
            } else {
                result = InstantiationResult.needRelated();
            }
        } else {
            result = InstantiationResult.nullValue();
        }

        return result;
    }

    public boolean lockChunksByIds(Collection<Integer> chunkIds) throws InterruptedException {
        // TODO: 28.06.2016 check constraints (chunk row/column count)
        return chunkMap.lockEntries(chunkIds, this::relatedChunks, this::instantiateChunk);
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
        Integer chunkId = ChunkIdGenerator.generateID(chunkSize, position);
        EndlessFieldChunk<T> chunk = chunkMap.getValue(chunkId);

        return chunk.get(position);
    }

    public Iterable<T> getCells(Iterable<CellPosition> positions) {
        ArrayList<T> result = new ArrayList<>();
        for (CellPosition position : positions) result.add(getCell(position));

        return result;
    }

    public ArrayList<T> getCellsByChunkId(Integer chunkId) {
        ArrayList<T> result = new ArrayList<>(chunkSize.cellCount());

        EndlessFieldChunk<T> chunk = chunkMap.getValue(chunkId);
        CellPosition origin = ChunkIdGenerator.chunkOrigin(chunkSize, chunkId);

        CellPosition position = new CellPosition();
        for (int row = 0; row < chunkSize.rowCount; row++) {
            for (int column = 0; column < chunkSize.columnCount; column++) {
                position.row = origin.row + row;
                position.column = origin.column + column;

                result.add(chunk.get(position));
            }
        }

        return result;
    }

    public LinkedHashMap<CellPosition, T> getEntries(Iterable<CellPosition> positions) {
        LinkedHashMap<CellPosition, T> result = new LinkedHashMap<>();
        for (CellPosition position : positions) result.put(position, getCell(position));

        return result;
    }

    public Map<CellPosition, T> getEntriesByChunkIds(Iterable<Integer> chunkIds) {
        HashMap<CellPosition, T> result = new HashMap<>();
        for (Integer chunkId : chunkIds) {
            EndlessFieldChunk<T> chunk = chunkMap.getValue(chunkId);
            result.putAll(chunk.cellsMap());
        }

        return result;
    }

    // It is possible that some of entries to update belongs to currently storing, or queued to store chunk.
    // In that case update actions will have no effect (we update existing db record here),
    // in this case cell will be stored with entire chunk during StoreChunkTask execution
    public void updateEntries(Map<CellPosition, ? extends EndlessFieldCell> entries) {
        // get chunk ids for updating cells
        Set<Integer> chunkIds = new HashSet<>();
        for (Map.Entry<CellPosition, ? extends EndlessFieldCell> entry : entries.entrySet()) {
            chunkIds.add(ChunkIdGenerator.generateID(chunkSize, entry.getKey()));
        }

        // increment update task counts used to prevent chunk removing before all its tasks are finished
        for (Integer chunkId : chunkIds) chunkMap.getValue(chunkId).updateTaskCount.incrementAndGet();

        LinkedHashMap<CellPosition, T> casted = new LinkedHashMap<>(entries.size());
        for (Map.Entry<CellPosition, ? extends EndlessFieldCell> entry : entries.entrySet()) {
            casted.put(entry.getKey(), ((T) entry.getValue()));
        }

        cellUpdateExec.submit(new UpdateCellTask<>(dataSource, chunkMap, casted, chunkIds));
    }

    public void removeChunk(Integer chunkId) throws InterruptedException {
        chunkMap.remove(chunkId);
    }

    public int size() {
        return chunkMap.size();
    }
}
