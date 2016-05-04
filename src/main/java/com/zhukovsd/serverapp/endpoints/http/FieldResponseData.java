package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.Gsonable;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

/**
 * Created by ZhukovSD on 29.04.2016.
 */
public class FieldResponseData<T extends EndlessFieldCell> {
    transient public ByteArrayOutputStream buffer;
    transient PrintWriter z;

    private int responseCode;
    private String msg;

    private LinkedHashMap<CellPosition, T> cells;

    FieldResponseData(PrintWriter a) {
        buffer = new ByteArrayOutputStream();
    }

    public FieldResponseData(PrintWriter a, LinkedHashMap<CellPosition, T> cells) {
        this(a);
        this.cells = cells;

        z = new PrintWriter(buffer);
//        z.write("hi there");
//        z.flush();

//        Gsonable.toJson(this, this.getClass(), z);
        test(this);
    }

    static <T extends EndlessFieldCell> void test(FieldResponseData<T> a) {
        Gsonable.toJson(a, a.z);
    }
}
