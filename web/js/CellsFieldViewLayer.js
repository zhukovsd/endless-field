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
 * Created by ZhukovSD on 15.06.2016.
 */

var CellsFieldViewLayer = function (fieldView, canvasId) {
    AbstractFieldViewLayer.call(this, fieldView, canvasId);

    this.drawCellsByChunkIds = function(chunkIds) {
        var fieldManager = this.fieldManager;

        var layer = this;
        // var scope = this.fieldView.camera.cellsScope();

        var a = 0;

        // set exact size
        var minRow = fieldManager.chunkIdFactor;
        var minColumn = fieldManager.chunkIdFactor;
        var maxRow = 0;
        var maxColumn = 0;
        console.log(JSON.stringify(chunkIds));
        chunkIds.forEach(function(chunkId) {
           // console.log(chunkId);

            var row = ChunkIdGenerator.chunkRow(chunkId, fieldManager.chunkIdFactor);
            var column = ChunkIdGenerator.chunkColumn(chunkId, fieldManager.chunkIdFactor);

            // console.log(chunkId + ": " + row + ", " + column);

            minRow = Math.min(minRow, row);
            minColumn = Math.min(minColumn, column);
            maxRow = Math.max(maxRow, row);
            maxColumn = Math.max(maxColumn, column);
        });
        var chunkRowRange = maxRow - minRow + 1;
        var chunkColumnRange = maxColumn - minColumn + 1;

        var chunkWidthInPixels = this.fieldManager.chunkSize.columnCount * this.fieldView.drawSettings.cellSize.width;
        var chunkHeightInPixels = this.fieldManager.chunkSize.rowCount * this.fieldView.drawSettings.cellSize.height;

        this.imageData.setSize(chunkColumnRange * chunkWidthInPixels + 1, chunkRowRange * chunkHeightInPixels + 1);

        var context = this.imageData.renderContext;
        context.strokeStyle = "black";
        context.font = "6pt Arial";
        context.lineWidth = 1;

        // console.log(
        //     chunkColumnRange * this.fieldManager.chunkSize.columnCount * this.fieldView.drawSettings.cellSize.width + ', ' +
        //     chunkRowRange * this.fieldManager.chunkSize.rowCount * this.fieldView.drawSettings.cellSize.height
        // );

        chunkIds.forEach(function(chunkId) {
            var origin = ChunkIdGenerator.chunkOrigin(fieldManager.chunkSize, fieldManager.chunkIdFactor, chunkId);

            for (var r = 0; r < fieldManager.chunkSize.rowCount; r++) {
                for (var c = 0; c < fieldManager.chunkSize.columnCount; c++) {
                    var row = origin.row + r;
                    var column = origin.column + c;

                    // if (scope.containsCell(row, column)) {
                    // var rect = layer.fieldView.camera.cellRect(row, column);

                    var cellSize = layer.fieldView.drawSettings.cellSize;
                    var rect = {
                        x: column * cellSize.width + 0.5,
                        y: row * cellSize.height + 0.5,
                        width: cellSize.width,
                        height: cellSize.height
                    };

                    // console.log("r = " + r + ", c = " + c + ", rect = " + JSON.stringify(rect));

                    layer.drawCell(rect, fieldManager.getCell(row, column), true);
                    a++;
                    // }
                }
            }
        });

        console.log(a + " cells drawn");

        var cameraPosition = fieldView.camera.position;
        var cameraColumn = ChunkIdGenerator.chunkColumn(cameraPosition.originChunkId, fieldManager.chunkIdFactor);
        var cameraRow = ChunkIdGenerator.chunkRow(cameraPosition.originChunkId, fieldManager.chunkIdFactor);

        // console.log((cameraRow - minRow) + ", " + (cameraColumn - minColumn));
        console.log(JSON.stringify(fieldView.camera.position));

        this.offset = {
            x: - (cameraPosition.shift.x + chunkWidthInPixels * (cameraColumn - minColumn)),
            y: - (cameraPosition.shift.y + chunkHeightInPixels * (cameraRow - minRow))
        };
    };
};

CellsFieldViewLayer.prototype = Object.create(AbstractFieldViewLayer.prototype);

CellsFieldViewLayer.prototype.drawCell = function(rect, cell, clear) {
    var c = this.imageData.renderContext;

    if (cell != null) {
        if (clear)
            c.clearRect(rect.x, rect.y, rect.width, rect.height);

        c.beginPath();
        c.rect(rect.x, rect.y, rect.width, rect.height);
        c.stroke();
        c.fillText(cell.text, rect.x + 2, rect.y + 9);
    }

    // console.log(JSON.stringify(rect));
};

// CellsFieldViewLayer.prototype.drawByPositions = function(positions) {
//     var fieldManager = this.fieldManager;
//
//     for (var key in positions) {
//         if (positions.hasOwnProperty(key)) {
//             var position = positions[key];
//
//             this.drawCell(
//                 this.fieldView.camera.cellRect(position.row, position.column),
//                 fieldManager.getCell(position.row, position.column),
//                 true
//             );
//         }
//     }
// };