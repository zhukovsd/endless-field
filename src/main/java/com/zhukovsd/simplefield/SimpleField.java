package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldCellFactory;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;

/**
 * Created by ZhukovSD on 20.03.2016.
 */
public class SimpleField extends EndlessField<SimpleFieldCell> {
    public SimpleField(int stripes, ChunkSize chunkSize, EndlessFieldDataSource<SimpleFieldCell> dataSource, EndlessFieldCellFactory<SimpleFieldCell> cellFactory) {
        super(stripes, chunkSize, dataSource, cellFactory);
    }

    public static void main(String[] args) {
        boolean f = SimpleField.class.isAssignableFrom(SimpleField.class);
        System.out.println(f);
    }
}
