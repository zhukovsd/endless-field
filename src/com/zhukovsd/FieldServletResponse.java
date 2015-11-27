package com.zhukovsd;

import com.google.gson.Gson;

import java.util.ArrayList;

class ResponseCell {
    int row, column;
    FieldCell cell;

    public ResponseCell(int row, int column, FieldCell cell) {
        this.row = row;
        this.column = column;
        this.cell = cell;
    }
}

/**
 * Created by ZhukovSD on 25.11.2015.
 */
public class FieldServletResponse {
    private ArrayList<ResponseCell> cells = new ArrayList<>();

    FieldServletResponse() { }

    void addCell(int row, int column, FieldCell cell) {
        cells.add(new ResponseCell(row, column, cell));
    }

    String toJson() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }
}
