package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldChunk;
import com.zhukovsd.serialization.Gsonable;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by ZhukovSD on 29.04.2016.
 */
public class FieldResponseData implements Gsonable {
    class ChunkData {
        CellPosition origin;
        ArrayList<EndlessFieldCell> cells;

        ChunkData(CellPosition origin, ArrayList<EndlessFieldCell> cells) {
            this.origin = origin;
            this.cells = cells;
        }
    }

    private int responseCode;
    private String msg;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    ArrayList<ChunkData> chunks = new ArrayList<>();

//    public FieldResponseData(Map<CellPosition, T> cells) {
//    public FieldResponseData(ArrayList<T> cells) {
        // TODO: 05.05.2016 specify response code
//        this.cells = cells;
//    }

    void addChunk(CellPosition origin, ArrayList<EndlessFieldCell> cells) {
        chunks.add(new ChunkData(origin, cells));
    }
}
