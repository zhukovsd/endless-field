/**
 * Created by ZhukovSD on 21.05.2016.
 */

var Scope = function(width, height, cameraPosition, cellSize) {
    // todo: min/max row/column constraints

    var leftVisibleColumnIndex = Math.ceil(cameraPosition.x / cellSize.width) - 1;
    // if mod > cell width, then this cell are not visible yet
    // if ((this.cameraPosition.x - 5) % 25 > 21) leftVisibleColumnIndex++;
    if (cameraPosition.x % cellSize.width == 0) leftVisibleColumnIndex++;
    if (leftVisibleColumnIndex < 0) leftVisibleColumnIndex = 0;

    var topVisibleRowIndex = Math.ceil(cameraPosition.y / cellSize.height) - 1;
    // // if mod > cell height, then this cell are not visible yet
    // if ((this.cameraPosition.y - 5) % 25 > 21) topVisibleRowIndex++;
    if (cameraPosition.y % cellSize.height == 0) topVisibleRowIndex++;
    if (topVisibleRowIndex < 0) topVisibleRowIndex = 0;

    var leftTopCellOriginPoint = {
        x: leftVisibleColumnIndex * cellSize.width - cameraPosition.x,
        y: topVisibleRowIndex * cellSize.height - cameraPosition.y
    };

    var visibleColumnCount = Math.ceil((width - leftTopCellOriginPoint.x) / cellSize.width);
    // ?
    // if ((width - leftTopCellOriginPoint.x) % cellSize.width == 0) visibleColumnCount++; 

    var visibleRowCount = Math.ceil((height - leftTopCellOriginPoint.y) / cellSize.height);
    // ?
    // if ((height - leftTopCellOriginPoint.y) % cellSize.height == 0) visibleRowCount++;

    this.origin = {row: topVisibleRowIndex, column: leftVisibleColumnIndex};
    this.rowCount = visibleRowCount;
    this.columnCount = visibleColumnCount;

    // //console.log(leftVisibleColumnIndex + ", " + topVisibleRowIndex + ", " + visibleColumnsCount);
    //
    // return {
    //     originRow: topVisibleRowIndex, originColumn: leftVisibleColumnIndex,
    //     rowCount: visibleRowsCount, columnCount: visibleColumnsCount
    // };
};

Scope.prototype = {
    chunkIds: function(chunkSize, chunkIdFactor) {
        var originChunkId = ChunkIdGenerator.generateId(chunkSize, chunkIdFactor, this.origin);
        
        var vChunkCount = Math.floor(this.origin.row / chunkSize.rowCount) * chunkSize.rowCount;
        vChunkCount = this.origin.row + this.rowCount - vChunkCount;
        vChunkCount = Math.floor(vChunkCount / chunkSize.rowCount);
        if (!((this.origin.row % chunkSize.rowCount == 0) && (this.rowCount % chunkSize.rowCount == 0)))
            vChunkCount++;

        var hChunkCount = Math.floor(this.origin.column / chunkSize.columnCount) * chunkSize.columnCount;
        hChunkCount = this.origin.column + this.columnCount - hChunkCount;
        hChunkCount = Math.floor(hChunkCount / chunkSize.columnCount);
        if (!((this.origin.column % chunkSize.columnCount == 0) && (this.columnCount % chunkSize.columnCount == 0)))
            hChunkCount++;

        var result = [];
        for (var chunkRow = 0; chunkRow < vChunkCount; chunkRow++) {
            for (var chunkColumn = 0; chunkColumn < hChunkCount; chunkColumn++) {
                result.push(originChunkId + chunkRow * chunkIdFactor + chunkColumn);
            }
        }
        
        return result;
    }
};