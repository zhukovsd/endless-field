/*
 * Copyright 2016 Zhukov Sergei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhukovsd.endlessfield.field;

import java.util.Collection;

/**
 * Created by ZhukovSD on 24.06.2016.
 */
public abstract class EndlessFieldChunkFactory<T extends EndlessFieldCell> {
    protected final EndlessField<T> field;

    public EndlessFieldChunkFactory(EndlessField<T> field) {
        this.field = field;
    }

    protected EndlessFieldChunk<T> generateChunk(Integer chunkId, Collection<Integer> lockedChunkIds) {
        return new EndlessFieldChunk<>(field.chunkSize.cellCount());
    };
}
