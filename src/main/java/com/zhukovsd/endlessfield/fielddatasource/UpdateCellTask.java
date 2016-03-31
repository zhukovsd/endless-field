package com.zhukovsd.endlessfield.fielddatasource;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

/**
 * Created by ZhukovSD on 29.03.2016.
 */
public class UpdateCellTask<T extends EndlessFieldCell> implements Runnable {
    EndlessFieldDataSource<T> dataSource;
    CellPosition position;
    T cell;

    public UpdateCellTask(EndlessFieldDataSource<T> dataSource, CellPosition position, T cell) {
        this.dataSource = dataSource;
        this.position = position;
        this.cell = cell;
    }

    @Override
    public void run() {
        // TODO: 23.03.2016 handle store exceptions / errors
        dataSource.modifyCell(position, cell);
        // commit stored state
    }
}
