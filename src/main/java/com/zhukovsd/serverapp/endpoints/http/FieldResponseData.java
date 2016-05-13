package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.serialization.Gsonable;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by ZhukovSD on 29.04.2016.
 */
public class FieldResponseData<T extends EndlessFieldCell> implements Gsonable {
    private int responseCode;
    private String msg;

//    private Map<CellPosition, T> cells;
    public ArrayList<T> cells;

//    public FieldResponseData(Map<CellPosition, T> cells) {
    public FieldResponseData(ArrayList<T> cells) {
        // TODO: 05.05.2016 specify response code
        this.cells = cells;
    }
}
