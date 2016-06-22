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
 * Created by ZhukovSD on 20.05.2016.
 */

var DrawSettings = function (cellWidth, cellHeight) {
    this.cellSize = {width: cellWidth, height: cellHeight};
};

var FieldView = function(fieldManager, drawSettings) {
    this.fieldManager = fieldManager;
    this.drawSettings = drawSettings;

    this.canvasContainer = null;
    this.canvas = null;
    this.canvasContext = null;
    
    this.camera = new Camera(this);
    
    this.init = function(containerId, canvasId) {
        this.canvasContainer = document.getElementById(containerId);
        this.canvas = document.getElementById(canvasId);
        this.canvasContext = this.canvas.getContext('2d');
        
        this.canvas.width = this.canvas.clientWidth;
        this.canvas.height = this.canvas.clientHeight;

        // this.canvasContext.fillStyle = "#cbcbcb";
        // this.canvasContext.strokeStyle = 'black';
        // this.canvasContext.lineWidth = 2;
        //
        // this.canvasContext.beginPath();
        // this.canvasContext.rect(5, 5, 25, 25);
        // this.canvasContext.stroke();
    };

    //

    this.drawCellsByChunkIds = function(chunkIds) {
        var fieldManager = this.fieldManager;
        var view = this;
        var scope = this.camera.cellsScope();

        //this.canvasContext.fillstyle = "black";
        this.canvasContext.strokeStyle = "black";
        this.canvasContext.font = "6pt Arial";
        this.canvasContext.lineWidth = 1;

        // fieldManager.chunkSize = {rowCount: 1, columnCount: 2};

        var a = 0;

        chunkIds.forEach(function(chunkId) {
            var origin = ChunkIdGenerator.chunkOrigin(fieldManager.chunkSize, fieldManager.chunkIdFactor, chunkId);

            for (var r = 0; r < fieldManager.chunkSize.rowCount; r++) {
                for (var c = 0; c < fieldManager.chunkSize.columnCount; c++) {
                    var row = origin.row + r;
                    var column = origin.column + c;
                    
                    if (scope.containsCell(row, column)) {
                        view.drawCell(view.camera.cellRect(row, column), fieldManager.getCell(row, column), true);
                        a++;
                    }
                }
            }
        });

        console.log(a + " cells drawn");
    };

    this.drawCellsByPositions = function(positions) {
        var fieldManager = this.fieldManager;

        for (var key in positions) {
            if (positions.hasOwnProperty(key)) {
                var position = positions[key];

                this.drawCell(
                    this.camera.cellRect(position.row, position.column),
                    fieldManager.getCell(position.row, position.column),
                    true
                );
            }
        }
    };

    //

    var view = this;
    this.fieldManager.onChunksReceived = function(chunkIds) {
        view.drawCellsByChunkIds(chunkIds);
    };
    
    this.fieldManager.onCellsUpdated = function (positions) {
        view.drawCellsByPositions(positions);
    }
};

FieldView.prototype = {
    drawCell: function(rect, cell, clear) {
        var c = this.canvasContext;
        
        if (cell != null) {
            if (clear)
                c.clearRect(rect.x, rect.y, rect.width, rect.height);
            
            c.beginPath();
            c.rect(rect.x, rect.y, rect.width, rect.height);
            c.stroke();
            c.fillText(cell.text, rect.x + 2, rect.y + 9);
        }
    }
};