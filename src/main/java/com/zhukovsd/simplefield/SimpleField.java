package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldActionInvoker;
import com.zhukovsd.endlessfield.field.EndlessFieldCellFactory;
import com.zhukovsd.endlessfield.field.EndlessFieldChunkFactory;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;

import java.util.Collections;
import java.util.Set;

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

    @Override
    protected EndlessFieldChunkFactory<SimpleFieldCell> createChunkFactory() {
        return new SimpleFieldChunkFactory(this);
    }

//    @Override
//    protected Set<Integer> relatedChunks(Integer chunkId) {
//        if (chunkId != 5)
//            return Collections.singleton(5);
//        else
//            return Collections.emptySet();
//    }
}
