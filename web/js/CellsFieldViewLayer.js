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
};

CellsFieldViewLayer.prototype = Object.create(AbstractFieldViewLayer.prototype);

CellsFieldViewLayer.prototype.renderByChunkIds = function(chunkIds) {
    // console.log('rendering chunks with ids = ' + chunkIds);
    var time = Date.now();

    var fieldManager = this.fieldManager;
    var chunkSize = fieldManager.chunkSize;
    var cellSize = this.fieldView.drawSettings.cellSize;

    var layer = this;

    var a = 0;

    var chunksArea = this.fieldView.currentChunkIdsArea();

    var chunkWidthInPixels = chunkSize.columnCount * cellSize.width;
    var chunkHeightInPixels = chunkSize.rowCount * cellSize.height;

    this.imageData.setSize(
        chunksArea.chunkColumnRange * chunkWidthInPixels + 1, chunksArea.chunkRowRange * chunkHeightInPixels + 1
    );

    var context = this.imageData.renderContext;
    context.strokeStyle = "black";
    context.font = "6pt Arial";
    context.lineWidth = 1;

    chunkIds.forEach(function(chunkId) {
        var origin = ChunkIdGenerator.chunkOrigin(fieldManager.chunkSize, fieldManager.chunkIdFactor, chunkId);

        for (var r = 0; r < fieldManager.chunkSize.rowCount; r++) {
            for (var c = 0; c < fieldManager.chunkSize.columnCount; c++) {
                var row = origin.row + r;
                var column = origin.column + c;

                var rect = {
                    x: (column - chunksArea.mostLeftColumn) * cellSize.width + 0.5,
                    y: (row - chunksArea.mostTopRow) * cellSize.height + 0.5,
                    width: cellSize.width,
                    height: cellSize.height
                };

                // console.log("r = " + r + ", c = " + c + ", rect = " + JSON.stringify(rect));

                layer.drawCell(rect, fieldManager.getCell(row, column), true);
                a++;
            }
        }
    });

    var cameraPosition = fieldView.camera.position;
    var cameraColumn = ChunkIdGenerator.chunkColumn(cameraPosition.originChunkId, fieldManager.chunkIdFactor);
    var cameraRow = ChunkIdGenerator.chunkRow(cameraPosition.originChunkId, fieldManager.chunkIdFactor);

    // console.log((cameraRow - minChunkRow) + ", " + (cameraColumn - minChunkColumn));
    // console.log(JSON.stringify(fieldView.camera.position));

    this.offset = {
        x: - (cameraPosition.shift.x + chunkWidthInPixels * (cameraColumn - chunksArea.minChunkColumn)),
        y: - (cameraPosition.shift.y + chunkHeightInPixels * (cameraRow - chunksArea.minChunkRow))
    };

    // console.log("cells layer offset = " + JSON.stringify(this.offset));
    console.log(a + " cells drawn, elapsed time = " + (Date.now() - time));
};

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