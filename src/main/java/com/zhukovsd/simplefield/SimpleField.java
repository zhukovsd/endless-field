package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.field.*;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;

/**
 * Created by ZhukovSD on 20.03.2016.
 */
public class SimpleField extends EndlessField<SimpleFieldCell> {
    public SimpleField(ChunkSize chunkSize, EndlessFieldDataSource<SimpleFieldCell> dataSource, EndlessFieldCellFactory<SimpleFieldCell> cellFactory) {
        super(chunkSize, dataSource, cellFactory);
    }
}
