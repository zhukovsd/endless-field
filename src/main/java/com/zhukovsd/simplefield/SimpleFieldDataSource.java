package com.zhukovsd.simplefield;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
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
    public static AtomicInteger runCounter = new AtomicInteger(0), loopCounter = new AtomicInteger(0), obsoleteCounter = new AtomicInteger(0);

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public SimpleFieldDataSource() {
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
            chunk.put(
                    new CellPosition(d.getInteger("row_index"), d.getInteger("column_index")),
                    new SimpleFieldCell(d.getBoolean("checked"))
            );
        }

        return chunk;
    }

    @Override
    public void storeChunk(EndlessFieldChunk<SimpleFieldCell> chunk, int chunkId) {
//        Document d = new Document()
        ArrayList<Document> cells = new ArrayList<>();

        for (Map.Entry<CellPosition, SimpleFieldCell> entry : chunk.entrySet()) {
            CellPosition position = entry.getKey();
            SimpleFieldCell cell = entry.getValue();

            // TODO: 23.03.2016 row & column indexes calculated from chunk index to be unique
            cells.add(new Document("row_index", position.row)
                    .append("column_index", position.column)
                    .append("chunk_id", chunkId)
                    .append("checked", cell.isChecked())
            );
        }

        // TODO: 21.03.2016 handle mongo exceptions
        collection.insertMany(cells);
    }

    @Override
    public void modifyCell(CellPosition position, SimpleFieldCell cell) {
        boolean isChecked = false;
        int a = 0, b = 0;

        runCounter.incrementAndGet();

        synchronized (cell) {
            isChecked = cell.isChecked();
            a = cell.a();
            b = cell.b();
        }

        // TODO: 30.03.2016 handle mongo exceptions
        collection.updateOne(
                and(eq("row_index", position.row), eq("column_index", position.column)),
                set("checked", isChecked)
        );
    }
}
