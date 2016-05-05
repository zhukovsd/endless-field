package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.Gsonable;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by ZhukovSD on 29.04.2016.
 */
public class FieldResponseData<T extends EndlessFieldCell> {
    private int responseCode;
    private String msg;

    private Map<CellPosition, T> cells;

    public FieldResponseData(Map<CellPosition, T> cells) {
        // TODO: 05.05.2016 specify response code
        this.cells = cells;
    }
}
