package com.zhukovsd.experiments.performance.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.EndlessFieldSizeConstraints;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
import com.zhukovsd.serverapp.endpoints.http.FieldResponseData;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCell;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;
import org.bson.*;

import java.io.*;
import java.util.*;

/**
 * Created by ZhukovSD on 10.05.2016.
 */
public class JsonSerializationPerformanceComparison {
    static String serializeWithGson(Gson gson, FieldResponseData data) {
        return gson.toJson(data);
//        return gson.toJson(data, new TypeToken<FieldResponseData<SimpleFieldCell>>() {}.getClass());
    }

    static String serializeManually(FieldResponseData data) {
        StringBuilder sb = new StringBuilder(50000);
        sb.append("{\"responseCode\":0,\"cells\":[");
        int c = 0;

//        for (SimpleFieldCell cell : data.cells) {
//            if (c != 0)
//                sb.append(",");
//
//            sb.append("{");
//
//            if (cell.isChecked()) {
//                sb.append("\"c\":");
//                sb.append(String.valueOf(cell.isChecked()));
//            }
//
//            sb.append("}");
//
//            c++;
//        }
//        sb.append("]}");

        return sb.toString();
    }

    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        SimpleField field = new SimpleField(
                10000,
                new ChunkSize(50, 50),
                new EndlessFieldSizeConstraints(40000, 40000),
                new EndlessFieldDataSource<SimpleFieldCell>() {
                    @Override
                    public boolean containsChunk(Integer chunkId) {
                        return false;
                    }

                    @Override
                    public EndlessFieldChunk<SimpleFieldCell> getChunk(Integer chunkId, ChunkSize chunkSize) {
                        return null;
                    }

                    @Override
                    public void storeChunk(EntryLockingConcurrentHashMap<Integer, EndlessFieldChunk<SimpleFieldCell>> chunkMap, int chunkId, EndlessFieldChunk<SimpleFieldCell> chunk) throws InterruptedException {
                        //
                    }

                    @Override
                    public void modifyEntries(Map<CellPosition, SimpleFieldCell> entries) {
                        //
                    }
                },
                new SimpleFieldCellFactory()
        );

        int chunkId = 0;
        field.lockChunksByIds(Collections.singletonList(0));
        ArrayList<SimpleFieldCell> cells;
        Random rand = new Random();
        try {
            cells = field.getCellsByChunkId(0);

            for (SimpleFieldCell cell : cells) {
                if (rand.nextBoolean()) {
                    cell.setChecked(true);
                }
            }
        } finally {
            field.unlockChunks();
        }

        //

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CustomizedTypeAdapterFactory())
//                .registerTypeAdapter(new TypeToken<ArrayList<SimpleFieldCell>>() {}.getType(), new ArrayAdapter())
//                .registerTypeAdapter(SimpleFieldCell.class, new Adapter())
//                .registerTypeAdapter(new TypeToken<FieldResponseData<SimpleFieldCell>>() {}.getType(), new DataAdapter())
//                .registerTypeAdapter(ArrayList.class, new ArrayAdapter())
//                .registerTypeAdapter(FieldResponseData.class, new DataAdapter())
//                .registerTypeAdapterFactory(new Factory())
                .create();

        ByteArrayOutputStream a = new ByteArrayOutputStream(50000);
        PrintWriter b = new PrintWriter(a);
        StringBuilder c = new StringBuilder(50000);

//        FieldResponseData<SimpleFieldCell> data = new FieldResponseData<>(cells);
        int count = 1;

//        try {
//            System.in.read();
//            System.out.println("started");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        long time = System.nanoTime();
        for (int i = 0; i < count; i++) {
//            String s = gson.toJson(cells, new TypeToken<ArrayList<SimpleFieldCell>>() {}.getType());
//            gson.toJson(cells, new TypeToken<FieldResponseData>() {}.getType(), c);
            gson.toJson(cells, c);

            System.out.println(c.toString());
//            String s = JsonSerializationPerformanceComparison.serializeWithGson(gson, data);
//            System.out.println(s);
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("gson = " + time);

        //

        time = System.nanoTime();
        for (int i = 0; i < count; i++) {
//            JsonSerializationPerformanceComparison.serializeManually(data);
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("manually = " + time);

        //

        time = System.nanoTime();
        for (int i = 0; i < count; i++) {
            BsonDocument document = new BsonDocument();
            BsonArray array = new BsonArray();
            for (SimpleFieldCell cell : cells) {
//                array.add(new BsonBoolean(cell.isChecked()));
                BsonDocument item = new BsonDocument();
                if (cell.isChecked())
                    item.put("c", new BsonBoolean(cell.isChecked()));

                array.add(item);
            }
            document.put("cells", array);

//            System.out.println(document.toJson(new JsonWriterSettings(JsonMode.SHELL)));
            String s = document.toJson();
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("bson = " + time);
    }
}

class Adapter extends TypeAdapter<SimpleFieldCell> {
    @Override
    public void write(JsonWriter out, SimpleFieldCell value) throws IOException {
        out.beginObject();
        if (value.isChecked())
            out.name("c").value(true);
        out.endObject();
    }

    @Override
    public SimpleFieldCell read(JsonReader in) throws IOException {
        return null;
    }
}

class ArrayAdapter extends TypeAdapter<ArrayList<SimpleFieldCell>> {
    @Override
    public void write(JsonWriter out, ArrayList<SimpleFieldCell> value) throws IOException {
        out.beginArray();
        for (SimpleFieldCell cell : value) {
            out.value(cell.isChecked());
        }
        out.endArray();
    }

    @Override
    public ArrayList<SimpleFieldCell> read(JsonReader in) throws IOException {
        return null;
    }
}

class DataAdapter extends TypeAdapter<FieldResponseData> {
    @Override
    public void write(JsonWriter out, FieldResponseData value) throws IOException {
        out.beginArray();
        out.endArray();
    }

    @Override
    public FieldResponseData read(JsonReader in) throws IOException {
        return null;
    }
}

class CustomizedTypeAdapterFactory implements TypeAdapterFactory {
//    private final Class<C> customizedClass;

    public CustomizedTypeAdapterFactory() {
//        this.customizedClass = customizedClass;
//        this.customizedClass = ((Class<C>) new TypeToken<ArrayList<SimpleFieldCell>>() {
//        }.getType());
    }

    @SuppressWarnings("unchecked") // we use a runtime check to guarantee that 'C' and 'T' are equal
    public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
//        return type.getRawType() == customizedClass
//                ? (TypeAdapter<T>) customizeMyClassAdapter(gson, (TypeToken<C>) type)
//                : null;

        return (TypeAdapter<T>) customizeMyClassAdapter(gson, (TypeToken<ArrayList<SimpleFieldCell>>) type);
    }

    private TypeAdapter<ArrayList<SimpleFieldCell>> customizeMyClassAdapter(Gson gson, TypeToken<ArrayList<SimpleFieldCell>> type) {
//        final TypeAdapter<ArrayList<SimpleFieldCell>> delegate = gson.getDelegateAdapter(this, type);
//        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
//        TypeAdapter<SimpleFieldCell> test = gson.getAdapter(SimpleFieldCell.class);
        return new TypeAdapter<ArrayList<SimpleFieldCell>>() {
            @Override public void write(JsonWriter out, ArrayList<SimpleFieldCell> value) throws IOException {
//                JsonElement tree = delegate.toJsonTree(value);
//                beforeWrite(value, tree);
//                JsonArray tree = new JsonArray();
//
//                for (SimpleFieldCell cell : value) {
////                    tree.add(test.toJsonTree(cell));
//                    JsonObject object = new JsonObject();
//                    object.addProperty("c", cell.isChecked());
//                    tree.add(object);
////                    tree.set(0, test.toJsonTree(cell));
//                }
//                elementAdapter.write(out, tree);

                out.beginArray();
                for (SimpleFieldCell cell : value) {
                    out.beginObject();
                    if (cell.isChecked())
                        out.name("c").value(true);
                    out.endObject();
                }
                out.endArray();
            }

            @Override public ArrayList<SimpleFieldCell> read(JsonReader in) throws IOException {
//                JsonElement tree = elementAdapter.read(in);
//                afterRead(tree);
//                return delegate.fromJsonTree(tree);

                return null;
            }
        };
    }

    /**
     * Override this to muck with {@code toSerialize} before it is written to
     * the outgoing JSON stream.
     */
//    protected void beforeWrite(C source, JsonElement toSerialize) {
//    }

    /**
     * Override this to muck with {@code deserialized} before it parsed into
     * the application type.
     */
//    protected void afterRead(JsonElement deserialized) {
//    }
}