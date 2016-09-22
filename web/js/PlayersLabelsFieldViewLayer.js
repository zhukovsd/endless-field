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

    this.mouseListener = null;
    this.renderedRects = [];
};

PlayersLabelsFieldViewLayer.prototype = Object.create(AbstractFieldViewLayer.prototype);

PlayersLabelsFieldViewLayer.prototype.initRenderCanvasStyleSettings = function() {
    // initialize render canvas with styles to render player label text
    var c = this.imageData.renderContext;
    c.fillStyle = "white";
    c.lineWidth = 0;
    c.font = "bold 15px Arial";
};

PlayersLabelsFieldViewLayer.prototype.calculateImageDataSizeAndShift = function(chunksScope) {
    this.imageData.setSize(chunksScope.widthInPixels, chunksScope.heightInPixels);
    this.chunksScopeImageDataOffset = {x: 0, y: -20};
};

inheritedRenderByChunkIds = PlayersLabelsFieldViewLayer.prototype.renderByChunkIds;
PlayersLabelsFieldViewLayer.prototype.renderByChunkIds = function(chunkIds) {
    this.renderedRects = [];
    inheritedRenderByChunkIds.call(this, chunkIds);
};

PlayersLabelsFieldViewLayer.prototype.rectByPosition = function(position, chunksScope) {
    if (this.fieldManager.playersPositions.containsKey(position)) {
        var name = this.fieldManager.playersPositions.value(position).name;

        var c = this.imageData.renderContext;
        var cellRect = chunksScope.cellRect(position);

        // TODO incapsulate in a method
        cellRect.x -= this.chunksScopeImageDataOffset.x;
        cellRect.y -= this.chunksScopeImageDataOffset.y;

        return {x: cellRect.x - 0.5, y: cellRect.y - 20.5, width: c.measureText(name).width + 10, height: 20};
    }
};

PlayersLabelsFieldViewLayer.prototype.renderByPosition = function(position, rect) {
    var hover = pointInRect(rect, this.absoluteMousePositionToRelative(this.mouseListener.mousePos));
    this.renderedRects.push({rect: rect, hover: hover});

    // console.log('!' + JSON.stringify(this.absoluteMousePositionToRelative(this.mouseListener.mousePos)));

    if (!hover) {
        // console.log('draw');

        var c = this.imageData.renderContext;

        // todo: check if mouse in rect
        c.save();
        c.fillStyle = "rgba(0, 0, 0, 0.5)";
        c.fillRect(rect.x, rect.y, rect.width, rect.height);
        c.restore();

        c.fillText(this.fieldManager.playersPositions.value(position).name, rect.x + 4, rect.y + 15);
    }
};

var pointInRect = function(rect, point) {
    return (point.x >= rect.x) && (point.x <= rect.x + rect.width)
        && (point.y >= rect.y) && (point.y <= rect.y + rect.height);
};

PlayersLabelsFieldViewLayer.prototype.doOnMouseMove = function(layerMousePosition) {
    var renderFlag = false;
    this.renderedRects.forEach(function(renderedRect) {
        if (!renderedRect.hover && pointInRect(renderedRect.rect, layerMousePosition)) {
            // renderedRect.hover = true;
            renderFlag = true;

            // console.log('mouse enter');
        } else if (renderedRect.hover && !pointInRect(renderedRect.rect, layerMousePosition)) {
            // renderedRect.hover = false;
            renderFlag = true;

            // console.log('mouse leave');
        }
    });

    if (renderFlag)
        this.refresh();
};