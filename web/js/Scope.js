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

var CellPosition = function(row, column) {
    this.row = row;
    this.column = column;
};

CellPosition.prototype = {
    toString: function() {
        return this.row + "," + this.column;
    },
    
    fromKey: function(key) {
        var coordinates = key.split(/[\s,]+/);
        if (coordinates.length == 2) {
            this.row = coordinates[0];
            this.column = coordinates[1];
        }
        
        return this;
    }
};

// todo: rename to camera scope?
var Scope = function(width, height, cameraPosition, cellSize, chunkSize, chunkIdFactor) {
    // todo: min/max row/column constraints
   
    var chunkOrigin = ChunkIdGenerator.chunkOrigin(chunkSize, chunkIdFactor, cameraPosition.originChunkId);

    var leftVisibleColumnIndex = chunkOrigin.column + Math.ceil(cameraPosition.shift.x / cellSize.width) - 1;
    // if mod > cell width, then this cell are not visible yet
    // if ((this.cameraPosition.x - 5) % 25 > 21) leftVisibleColumnIndex++;
    if (cameraPosition.shift.x % cellSize.width == 0) leftVisibleColumnIndex++;
    if (leftVisibleColumnIndex < 0) leftVisibleColumnIndex = 0;

    var topVisibleRowIndex = chunkOrigin.row + Math.ceil(cameraPosition.shift.y / cellSize.height) - 1;
    // // if mod > cell height, then this cell are not visible yet
    // if ((this.cameraPosition.y - 5) % 25 > 21) topVisibleRowIndex++;
    if (cameraPosition.shift.y % cellSize.height == 0) topVisibleRowIndex++;
    if (topVisibleRowIndex < 0) topVisibleRowIndex = 0;

    var leftTopCellOriginPoint = {
        x: (leftVisibleColumnIndex - chunkOrigin.column) * cellSize.width - cameraPosition.shift.x,
        y: (topVisibleRowIndex - chunkOrigin.row) * cellSize.height - cameraPosition.shift.y
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

    // todo: remove method?
    this.removePartiallyVisibleCells = function() {
        if (cameraPosition.shift.y % cellSize.height != 0) {
            this.origin.row++;
            this.rowCount--;
        }

        if (height - cameraPosition.shift.y % cellSize.height !=0) {
            this.rowCount--;
        }

        if (cameraPosition.shift.x % cellSize.width != 0) {
            this.origin.column++;
            this.columnCount--;
        }

        if (width - cameraPosition.shift.x % cellSize.width !=0) {
            this.columnCount--;
        }
    }
};

Scope.prototype = {
    chunkIds: function(chunkSize, chunkIdFactor) {
        var originChunkId = ChunkIdGenerator.generateId(chunkSize, chunkIdFactor, this.origin);

        var bottomRightScopePosition = new CellPosition(
            this.origin.row + this.rowCount - 1, this.origin.column + this.columnCount - 1
        );
        var rightBottomChunkId = ChunkIdGenerator.generateId(chunkSize, chunkIdFactor, bottomRightScopePosition);

        var vChunkCount = ChunkIdGenerator.chunkRow(rightBottomChunkId, chunkIdFactor)
            - ChunkIdGenerator.chunkRow(originChunkId, chunkIdFactor) + 1;

        var hChunkCount = ChunkIdGenerator.chunkColumn(rightBottomChunkId, chunkIdFactor)
            - ChunkIdGenerator.chunkColumn(originChunkId, chunkIdFactor) + 1;

        var result = [];
        for (var chunkRow = 0; chunkRow < vChunkCount; chunkRow++) {
            for (var chunkColumn = 0; chunkColumn < hChunkCount; chunkColumn++) {
                result.push(originChunkId + chunkRow * chunkIdFactor + chunkColumn);
            }
        }
        
        return result;
    },

    // todo: remove method?
    containsCell: function(row, column) {
        var result = true;

        if ((row < this.origin.row) || (column < this.origin.column)) result = false;
        if (row >= this.origin.row + this.rowCount) result = false;
        if (column >= this.origin.column + this.columnCount) result = false;

        return result;
    },

    equals: function(scope) {
        var result = true;

        if (this.origin.row != scope.origin.row) return false;
        if (this.origin.column != scope.origin.column) return false;
        if (this.rowCount != scope.rowCount) return false;
        if (this.columnCount != scope.columnCount) return false;        
        
        return result;
    },

    expand: function(fieldView, x, y) {
        var cellSize = fieldView.drawSettings.cellSize;
        var chunkSize = fieldView.fieldManager.chunkSize;
        var idFactor = fieldView.fieldManager.chunkIdFactor;

        // console.log('current camera position = ' + JSON.stringify(fieldView.camera.position));

        var topLeftCameraPosition = fieldView.camera.position.shiftBy(
            {x: x, y: y}, chunkSize, idFactor, cellSize
        );
        // var o = ChunkIdGenerator.chunkOrigin(chunkSize, idFactor, topLeftCameraPosition.originChunkId);
        // var b = o.column + Math.ceil(topLeftCameraPosition.shift.x / cellSize.width) - 1;
        // var c = o.row + Math.ceil(topLeftCameraPosition.shift.y / cellSize.height) - 1;

        // console.log('top left camera position = ' + JSON.stringify(topLeftCameraPosition) + ', ' + b + ' ' + c);

        var bottomRightCameraPosition = fieldView.camera.position.shiftBy(
            {x: - (fieldView.width() + x), y: - (fieldView.height() + y)},
            chunkSize, idFactor, cellSize
        );

        // var o = ChunkIdGenerator.chunkOrigin(chunkSize, idFactor, bottomRightCameraPosition.originChunkId);
        // var b = o.column + Math.ceil(bottomRightCameraPosition.shift.x / cellSize.width) - 1;
        // var c = o.row + Math.ceil(bottomRightCameraPosition.shift.y / cellSize.height) - 1;

        // console.log('right bottom = ' + JSON.stringify(bottomRightCameraPosition) + ', ' + b + ' ' + c);

        var offset = bottomRightCameraPosition.calculateMouseOffset(topLeftCameraPosition, chunkSize, idFactor, cellSize);

        // console.log('offset = ' + JSON.stringify(offset));

        return new Scope(offset.x, offset.y, topLeftCameraPosition, cellSize, chunkSize, idFactor);
    },

    toSet: function() {
        var result = {};

        for (var row = 0; row < this.rowCount; row++) {
            for (var column = 0; column < this.columnCount; column++) {
                // var key = (this.origin.row + row) + "," + (this.origin.column + column);

                var position = new CellPosition(this.origin.row + row, this.origin.column + column);
                result[position.toString()] = position;
            }
        }

        return result;
    },

    difference: function(scope) {
        var scope1 = this.toSet();
        var scope2 = scope.toSet();

        for (var key in scope2)
            //noinspection JSUnfilteredForInLoop
            delete scope1[key];
        
        return scope1;
    }
};