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
        var scope = this.fieldView.camera.cellsScope();

        //this.canvasContext.fillstyle = "black";
        this.context.strokeStyle = "black";
        this.context.font = "6pt Arial";
        this.context.lineWidth = 1;

        // fieldManager.chunkSize = {rowCount: 1, columnCount: 2};

        var a = 0;

        chunkIds.forEach(function(chunkId) {
            var origin = ChunkIdGenerator.chunkOrigin(fieldManager.chunkSize, fieldManager.chunkIdFactor, chunkId);

            for (var r = 0; r < fieldManager.chunkSize.rowCount; r++) {
                for (var c = 0; c < fieldManager.chunkSize.columnCount; c++) {
                    var row = origin.row + r;
                    var column = origin.column + c;

                    if (scope.containsCell(row, column)) {
                        layer.drawCell(layer.fieldView.camera.cellRect(row, column), fieldManager.getCell(row, column), true);
                        a++;
                    }
                }
            }
        });

        console.log(a + " cells drawn");
    };
};

CellsFieldViewLayer.prototype = Object.create(AbstractFieldViewLayer.prototype);

CellsFieldViewLayer.prototype.drawByPositions = function(positions) {
    var fieldManager = this.fieldManager;

    for (var key in positions) {
        if (positions.hasOwnProperty(key)) {
            var position = positions[key];

            this.drawCell(
                this.fieldView.camera.cellRect(position.row, position.column),
                fieldManager.getCell(position.row, position.column),
                true
            );
        }
    }
};

CellsFieldViewLayer.prototype.drawCell = function(rect, cell, clear) {
    var c = this.context;

    if (cell != null) {
        if (clear)
            c.clearRect(rect.x, rect.y, rect.width, rect.height);

        c.beginPath();
        c.rect(rect.x, rect.y, rect.width, rect.height);
        c.stroke();
        c.fillText(cell.text, rect.x + 2, rect.y + 9);
    }
};