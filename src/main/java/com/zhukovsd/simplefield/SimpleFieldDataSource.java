package com.zhukovsd.simplefield;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.enrtylockingconcurrenthashmap.StripedEntryLockingConcurrentHashMap;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * Created by ZhukovSD on 20.03.2016.
 */
public class SimpleFieldDataSource implements EndlessFieldDataSource<SimpleFieldCell> {
    // TODO: 03.04.2016 remove debug
    public static AtomicInteger runCounter = new AtomicInteger(0), loopCounter = new AtomicInteger(0), obsoleteCounter = new AtomicInteger(0);

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public SimpleFieldDataSource() {
        // TODO: 01.05.2016 move settings to web app configuration
        mongoClient = new MongoClient("localhost", 27017);
        database = mongoClient.getDatabase("simple");
        collection = database.getCollection("field");
    }

    @Override
    public boolean containsChunk(Integer chunkId) {
        // TODO: 21.03.2016 handle mongo exceptions
        return (collection.count(eq("chunk_id", chunkId)) != 0);
    }

    @Override
    public EndlessFieldChunk<SimpleFieldCell> getChunk(Integer chunkId, ChunkSize chunkSize) {
        EndlessFieldChunk<SimpleFieldCell> chunk = new EndlessFieldChunk<>(chunkSize.cellCount());

        // TODO: 21.03.2016 handle mongo exceptions
        FindIterable<Document> findIterable = collection.find(eq("chunk_id", chunkId));
        for (Document d : findIterable) {
            // TODO: 21.03.2016 check is cellDocument contains necessary fields
            SimpleFieldCell cell = new SimpleFieldCell(d.getBoolean("checked"));
//            cell.s = d.getInteger("row_index") + "," + d.getInteger("column_index");
            chunk.put(
                    new CellPosition(d.getInteger("row_index"), d.getInteger("column_index")),
                    cell
            );
        }

        return chunk;
    }

    @Override
    public void storeChunk(StripedEntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<SimpleFieldCell>> chunkMap,
                           int chunkId, EndlessFieldChunk<SimpleFieldCell> chunk) throws InterruptedException
    {
        ArrayList<Document> cells = new ArrayList<>();

        // if chunk was not locked, it's not exists in the chunk map. it is possible, when it was not yet added,
        // because this store thread and this storeChunk() method was called in context of web container thread pool
        // thread due to FixedExecutorService reject policy. If chunk is not exists in the map, we don't really
        // need to lock it, because it's already locked in EndlessField.provideAndLock() which is below this method
        // in call stack (only after rejection)
        boolean isLocked = chunkMap.lockKey(chunkId);
        try {
            for (Map.Entry<CellPosition, SimpleFieldCell> entry : chunk.cellsMap().entrySet()) {
                CellPosition position = entry.getKey();
                SimpleFieldCell cell = entry.getValue();

                // TODO: 23.03.2016 row & column indexes calculated from chunk index to be unique
                cells.add(new Document("row_index", position.row)
                        .append("column_index", position.column)
                        .append("chunk_id", chunkId)
                        .append("checked", cell.isChecked())
                );
            }
        } finally {
            if (isLocked) {
                chunkMap.unlock();
            }
        }

        // TODO: 21.03.2016 handle mongo exceptions
        try {
            collection.insertMany(cells);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifyEntries(Map<CellPosition, SimpleFieldCell> entries) {
        runCounter.incrementAndGet();

        for (Map.Entry<CellPosition, SimpleFieldCell> entry : entries.entrySet()) {
            SimpleFieldCell cell = entry.getValue();
            CellPosition position = entry.getKey();

            boolean isChecked = false;

            // synchronize on cell to protect cell fields consistency and prevent cell saving in the middle of
            // its modifying
            synchronized (cell) {
                isChecked = cell.isChecked();
//                a = cell.a();
//                b = cell.b();
            }

            try {
                // TODO: 30.03.2016 handle mongo exceptions
                collection.updateOne(
                        and(eq("row_index", position.row), eq("column_index", position.column)),
                        set("checked", isChecked)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
