package com.zhukovsd;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by ZhukovSD on 25.11.2015.
 */
public class FieldServletResponse {
    private ArrayList<FieldCell> cells;

    FieldServletResponse(ArrayList<FieldCell> cells) {
        this.cells = cells;
    }

    String toJson() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }
}
