package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

import java.util.ArrayList;

/**
 * Created by ZhukovSD on 29.04.2016.
 */
public class FieldResponseData {
    public class ChunkData {
        public CellPosition origin;
        public ArrayList<EndlessFieldCell> cells;

        ChunkData(CellPosition origin, ArrayList<EndlessFieldCell> cells) {
            this.origin = origin;
            this.cells = cells;
        }
    }

    private int responseCode;
    private String msg;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public ArrayList<ChunkData> chunks = new ArrayList<>();

//    public FieldResponseData(Map<CellPosition, T> cells) {
//    public FieldResponseData(ArrayList<T> cells) {
        // TODO: 05.05.2016 specify response code
//        this.cells = cells;
//    }

    void addChunk(CellPosition origin, ArrayList<EndlessFieldCell> cells) {
        chunks.add(new ChunkData(origin, cells));
    }
}
