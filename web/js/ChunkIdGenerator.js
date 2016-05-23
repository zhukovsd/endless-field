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