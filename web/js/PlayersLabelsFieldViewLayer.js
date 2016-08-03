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
 * Created by ZhukovSD on 16.06.2016.
 */

var PlayersLabelsFieldViewLayer = function(fieldView, canvasId) {
    AbstractFieldViewLayer.call(this, fieldView, canvasId);

    // this.drawVisiblePlayersLabels = function() {
    //     this.clear();
    //     var scope = this.fieldView.camera.cellsScope();
    //
    //     for (var userId in this.fieldManager.playersPositions) {
    //         if (this.fieldManager.playersPositions.hasOwnProperty(userId)) {
    //             var position = this.fieldManager.playersPositions[userId].position;
    //             var player = this.fieldManager.playersPositions[userId].player;
    //
    //             if (scope.containsCell(position.row, position.column)) {
    //                 // console.log(this.fieldManager.players[userId].name);
    //                 this.drawPlayerLabel(position, player.name);
    //             }
    //         }
    //     }
    // }
};

PlayersLabelsFieldViewLayer.prototype = Object.create(AbstractFieldViewLayer.prototype);

PlayersLabelsFieldViewLayer.prototype.initRenderCanvasStyleSettings = function() {
    var c = this.imageData.renderContext;
    // c.strokeStyle = "white";
    c.fillStyle = "white";
    c.lineWidth = 0;
    c.font = "bold 15px Arial";
    // c.fillStyle = "rgba(0, 0, 0, 0.5)";
};

PlayersLabelsFieldViewLayer.prototype.rectByPosition = function(position, chunksArea) {
    var f = false;
    var name;
    for (var userId in this.fieldManager.playersPositions) {
        if (this.fieldManager.playersPositions.hasOwnProperty(userId)) {
            var p = this.fieldManager.playersPositions[userId].position;

            if (p.toString() == position.toString()) {
                f = true;
                name = this.fieldManager.playersPositions[userId].player.name;

                break;
            }
        }
    }

    console.log(JSON.stringify(this.fieldManager.playersPositions));

    if (f) {
        // console.log('123');

        var c = this.imageData.renderContext;
        var cellRect = this.fieldView.camera.cellRect(position.row, position.column);

        return {x: cellRect.x - 0.5, y: cellRect.y - 20.5, width: c.measureText(name).width + 10, height: 20};
    } else {
        return null;
    }
};

PlayersLabelsFieldViewLayer.prototype.renderByPosition = function(position, rect) {
    console.log('sup');

    var c = this.imageData.renderContext;

    // c.strokeStyle = "white";
    // c.lineWidth = 0;
    // c.font = "bold 15px Arial";
    // c.fillStyle = "rgba(0, 0, 0, 0.5)";
    // c.fillStyle = "black";

    // c.beginPath();
    c.save();
    c.fillStyle = "rgba(0, 0, 0, 0.5)";
    c.fillRect(rect.x, rect.y, rect.width, rect.height);
    c.restore();

    c.fillText('User #0', rect.x + 5, rect.y + 15);
};

// PlayersLabelsFieldViewLayer.prototype.drawByPositions = function(positions) {
//     this.clear();
//     this.drawVisiblePlayersLabels();
// };
//
// PlayersLabelsFieldViewLayer.prototype.drawPlayerLabel = function(cellPosition, name) {
//     var c = this.context;
//     var cellRect = this.fieldView.camera.cellRect(cellPosition.row, cellPosition.column);
//
//     c.strokeStyle = "white";
//     c.lineWidth = 0;
//     c.font = "bold 15px Arial";
//     c.fillStyle = "rgba(0, 0, 0, 0.5)";
//     // c.fillStyle = "black";
//
//     var rect = {x: cellRect.x - 0.5, y: cellRect.y - 20.5, width: c.measureText(name).width + 10, height: 20};
//
//     // c.beginPath();
//     c.fillRect(rect.x, rect.y, rect.width, rect.height);
//     // c.fill();
//
//     c.fillStyle = "white";
//     c.fillText(name, rect.x + 5, rect.y + 15);
// };