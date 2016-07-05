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

package com.zhukovsd.entrylockingconcurrenthashmap;

/**
 * Created by ZhukovSD on 25.06.2016.
 */
public class InstantiationResult<V> {
    final public InstantiationResultType type;
    final public V value;

    private InstantiationResult(InstantiationResultType type, V value) {
        this.type = type;
        this.value = value;
    }

    public static <V> InstantiationResult<V> provided(V value) {
        return new InstantiationResult<>(InstantiationResultType.PROVIDED, value);
    }

    public static <V> InstantiationResult<V> nullValue() {
        return new InstantiationResult<>(InstantiationResultType.NULL, null);
    }

    public static <V> InstantiationResult<V> needRelated() {
        return new InstantiationResult<>(InstantiationResultType.NEED_RELATED_VALUE, null);
    }
}