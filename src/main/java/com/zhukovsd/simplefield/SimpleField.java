package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldActionInvoker;
import com.zhukovsd.endlessfield.field.EndlessFieldCellFactory;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;

/**
 * Created by ZhukovSD on 20.03.2016.
 */
public class SimpleField extends EndlessField<SimpleFieldCell> {
    public SimpleField(int stripes, ChunkSize chunkSize, EndlessFieldDataSource<SimpleFieldCell> dataSource, EndlessFieldCellFactory<SimpleFieldCell> cellFactory) {
        super(stripes, chunkSize, dataSource, cellFactory);
    }

    @Override
    protected EndlessFieldActionInvoker createActionInvoker() {
        return new SimpleFieldActionInvoker(this);
    }
}
