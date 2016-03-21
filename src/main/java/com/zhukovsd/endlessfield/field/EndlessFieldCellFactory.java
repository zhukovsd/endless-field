package com.zhukovsd.endlessfield.field;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public interface EndlessFieldCellFactory<T extends EndlessFieldCell> {
    T create();
}
