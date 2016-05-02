package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

import java.util.LinkedHashMap;

/**
 * Created by ZhukovSD on 29.04.2016.
 */
public class FieldResponseData<T extends EndlessFieldCell> {
    private int responseCode;
    private String msg;

    private LinkedHashMap<CellPosition, T> cells;

    public FieldResponseData(LinkedHashMap<CellPosition, T> cells) {
        this.cells = cells;
    }
}
