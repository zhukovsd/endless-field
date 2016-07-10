package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.EndlessFieldSizeConstraints;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldActionInvoker;
import com.zhukovsd.endlessfield.field.EndlessFieldChunkFactory;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;

/**
 * Created by ZhukovSD on 20.03.2016.
 */
public class SimpleField extends EndlessField<SimpleFieldCell> {
    public SimpleField(
            int stripes, ChunkSize chunkSize, EndlessFieldSizeConstraints sizeConstraints,
            EndlessFieldDataSource<SimpleFieldCell> dataSource
    ) {
        super(stripes, chunkSize, sizeConstraints, dataSource);
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
