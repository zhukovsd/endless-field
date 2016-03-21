package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.field.EndlessFieldCellFactory;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public class SimpleFieldCellFactory implements EndlessFieldCellFactory<SimpleFieldCell> {
    @Override
    public SimpleFieldCell create() {
        return new SimpleFieldCell(false);
    }
}
