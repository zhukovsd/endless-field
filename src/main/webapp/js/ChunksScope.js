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
 * Created by ZhukovSD on 04.08.2016.
 */

var ChunksScope = function(fieldView, chunkIds) {
    var fieldManager = fieldView.fieldManager;
    
    var chunkSize = fieldManager.chunkSize;
    var cellSize = fieldView.drawSettings.cellSize;

    var minChunkRow = fieldManager.chunkIdFactor;
    var minChunkColumn = fieldManager.chunkIdFactor;
    var maxChunkRow = 0;
    var maxChunkColumn = 0;

    var chunkWidthInPixels = chunkSize.columnCount * cellSize.width;
    var chunkHeightInPixels = chunkSize.rowCount * cellSize.height;

    chunkIds.forEach(function(chunkId) {
        var chunkRow = ChunkIdGenerator.chunkRow(chunkId, fieldManager.chunkIdFactor);
        var chunkColumn = ChunkIdGenerator.chunkColumn(chunkId, fieldManager.chunkIdFactor);

        minChunkRow = Math.min(minChunkRow, chunkRow);
        minChunkColumn = Math.min(minChunkColumn, chunkColumn);
        maxChunkRow = Math.max(maxChunkRow, chunkRow);
        maxChunkColumn = Math.max(maxChunkColumn, chunkColumn);
    });

    var chunkRowRange = maxChunkRow - minChunkRow + 1;
    var chunkColumnRange = maxChunkColumn - minChunkColumn + 1;

    this.chunkWidthInPixels = chunkWidthInPixels;
    this.chunkHeightInPixels = chunkHeightInPixels;

    this.widthInPixels = chunkColumnRange * chunkWidthInPixels + 1;
    this.heightInPixels = chunkRowRange * chunkHeightInPixels + 1;

    this.minChunkRow = minChunkRow;
    this.minChunkColumn = minChunkColumn;

    this.chunkRowRange = chunkRowRange;
    this.chunkColumnRange = chunkColumnRange;

    this.mostTopRow = minChunkRow * fieldManager.chunkSize.rowCount;
    this.mostLeftColumn = minChunkColumn * fieldManager.chunkSize.columnCount;
};

ChunksScope.prototype = {
    cellRect: function(position) {
        var cellSize = fieldView.drawSettings.cellSize;

        return {
            // since canvas calculates from the half of a pixel, add 0.5 to prevent anti-aliasing
            x: (position.column - this.mostLeftColumn) * cellSize.width + 0.5,
            y: (position.row - this.mostTopRow) * cellSize.height + 0.5,
            width: cellSize.width,
            height: cellSize.height
        };
    }
};