package com.zhukovsd.endlessfield.field;

import java.util.Map;

/**
 * Created by ZhukovSD on 23.03.2016.
 */
public class CellEntry<T extends EndlessFieldCell> {
    public final CellPosition position;
    public final T cell;

    public CellEntry(CellPosition position, T cell) {
        this.position = position;
        this.cell = cell;
    }
}
