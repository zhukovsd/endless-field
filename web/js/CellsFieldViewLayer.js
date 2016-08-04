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

CellsFieldViewLayer.prototype.initRenderCanvasStyleSettings = function() {
    var c = this.imageData.renderContext;
    c.strokeStyle = "black";
    c.font = "6pt Arial";
    c.lineWidth = 1;
};

CellsFieldViewLayer.prototype.rectByPosition = function(position, chunksScope) {
    var cellSize = this.fieldView.drawSettings.cellSize;

    return {
        x: (position.column - chunksScope.mostLeftColumn) * cellSize.width + 0.5,
        y: (position.row - chunksScope.mostTopRow) * cellSize.height + 0.5,
        width: cellSize.width,
        height: cellSize.height
    };
};

CellsFieldViewLayer.prototype.renderByPosition = function(position, rect) {
    this.drawCell(rect, fieldManager.getCell(position.row, position.column), true);
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