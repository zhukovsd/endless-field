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

/**
 * Created by ZhukovSD on 21.05.2016.
 */

var ChunkIdGenerator = function() {};

ChunkIdGenerator.generateId = function(chunkSize, idFactor, position) {
    return Math.floor(position.row / chunkSize.rowCount) * idFactor + Math.floor(position.column / chunkSize.columnCount);
};

ChunkIdGenerator.chunkOrigin = function(chunkSize, idFactor, chunkId) {
    return {
        row: Math.floor(chunkId / idFactor) * chunkSize.rowCount,
        column: (chunkId % idFactor) * chunkSize.columnCount
    };
};

ChunkIdGenerator.chunkRow = function(chunkId, idFactor) {
    return Math.floor(chunkId / idFactor);
};

ChunkIdGenerator.chunkColumn = function(chunkId, idFactor) {
    return chunkId % idFactor;
};