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

package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

import java.util.ArrayList;

/**
 * Created by ZhukovSD on 29.04.2016.
 */
public class FieldResponseData {
    public class ChunkData {
        public CellPosition origin;
        public ArrayList<EndlessFieldCell> cells;

        ChunkData(CellPosition origin, ArrayList<EndlessFieldCell> cells) {
            this.origin = origin;
            this.cells = cells;
        }
    }

    private int responseCode;
    private String msg;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public ArrayList<ChunkData> chunks = new ArrayList<>();

//    public FieldResponseData(Map<CellPosition, T> cells) {
//    public FieldResponseData(ArrayList<T> cells) {
        // TODO: 05.05.2016 specify response code
//        this.cells = cells;
//    }

    void addChunk(CellPosition origin, ArrayList<EndlessFieldCell> cells) {
        chunks.add(new ChunkData(origin, cells));
    }
}
