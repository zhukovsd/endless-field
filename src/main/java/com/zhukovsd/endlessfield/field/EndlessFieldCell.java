package com.zhukovsd.endlessfield.field;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public abstract class EndlessFieldCell {
    public EndlessFieldCell() {

    }

    protected EndlessFieldCell(EndlessFieldCell source) {

    };

    public abstract EndlessCellCloneFactory getFactory();
}
