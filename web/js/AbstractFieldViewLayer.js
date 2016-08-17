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

var AbstractFieldViewLayer = function(fieldView, canvasId) {
    this.fieldView = fieldView;
    this.fieldManager = fieldView.fieldManager;

    this.canvas = null;
    this.context = null;

    this.width = null;
    this.height = null;

    // var imageData = null;
    this.imageData = null;
    this.offset = {x: 0, y: 0};
    var storedOffset;

    this.displayedChunkIds = [];
    
    var layer = this;
    window.addEventListener('load',
        function(event) {
            layer.canvas = document.getElementById(canvasId);
            layer.context = layer.canvas.getContext('2d');

            layer.imageData = new FieldViewLayerImageData(layer);
            layer.applyCanvasSize();

            // ?
            // offset = {x: -(layer.width * 0.25), y: -(layer.height * 0.25)};
        }, false
    );

    this.applyCanvasSize = function() {
        if (this.canvas !== null) {
            this.canvas.width = this.width;
            this.canvas.height = this.height;
            
            // this.imageData.setSize(this.width, this.height);
        }
    };

    this.setSize = function(width, height) {
        this.width = width;
        this.height = height;

        this.applyCanvasSize();
    };

    this.clear = function() {
        if (this.context !== null) {
            this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        }
    };

    this.display = function() {
        this.clear();
        // this.context.putImageData(imageData.data, offset.x, offset.y);
        this.context.drawImage(this.imageData.renderCanvas, this.offset.x, this.offset.y);
    };

    this.storeOffset = function() {
        storedOffset = { x: this.offset.x, y: this.offset.y };
    };

    this.shiftOffset = function(offsetDelta) {
        this.offset.x = storedOffset.x + offsetDelta.x;
        this.offset.y = storedOffset.y + offsetDelta.y;

        this.display();
    };
};

AbstractFieldViewLayer.prototype.initRenderCanvasStyleSettings = function() {
    console.log('abstract initRenderCanvasStyleSettings()');
};

AbstractFieldViewLayer.prototype.rectByPosition = function(position, chunksScope) {
    // console.log('abstract rectByPosition()');
};

AbstractFieldViewLayer.prototype.locateAndRenderByPosition = function(position, chunksScope) {
    var rect = this.rectByPosition(position, chunksScope);

    if (rect != undefined)
    {
        // todo: don't render positions which not in chunksScope
        this.renderByPosition(position, rect);
    }
};

AbstractFieldViewLayer.prototype.renderByPosition = function(position, rect) {
    // console.log('abstract renderByPosition()');
};

AbstractFieldViewLayer.prototype.renderByPositions = function(positions) {
    var chunksScope = this.fieldView.currentChunksScope();

    for (var key in positions) {
        if (positions.hasOwnProperty(key)) {
            var position = positions[key];

            this.locateAndRenderByPosition(position, chunksScope);
        }
    }
};

AbstractFieldViewLayer.prototype.renderByChunkIds = function(chunkIds) {
    var time = Date.now();

    this.displayedChunkIds = chunkIds;

    var fieldManager = this.fieldManager;
    var layer = this;

    var chunksScope = this.fieldView.currentChunksScope();

    // console.log(JSON.stringify(chunksScope));

    this.imageData.setSize(
        // chunksScope.chunkColumnRange * chunkWidthInPixels + 1, chunksScope.chunkRowRange * chunkHeightInPixels + 1
        chunksScope.widthInPixels, chunksScope.heightInPixels
    );

    var a = 0;

    chunkIds.forEach(function(chunkId) {
        var origin = ChunkIdGenerator.chunkOrigin(fieldManager.chunkSize, fieldManager.chunkIdFactor, chunkId);
        var position = new CellPosition();

        for (var r = 0; r < fieldManager.chunkSize.rowCount; r++) {
            for (var c = 0; c < fieldManager.chunkSize.columnCount; c++) {
                position.row = origin.row + r;
                position.column = origin.column + c;

                layer.locateAndRenderByPosition(position, chunksScope);

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
        x: - (cameraPosition.shift.x + chunksScope.chunkWidthInPixels * (cameraColumn - chunksScope.minChunkColumn)),
        y: - (cameraPosition.shift.y + chunksScope.chunkHeightInPixels * (cameraRow - chunksScope.minChunkRow))
    };

    // console.log("cells layer offset = " + JSON.stringify(this.offset));
    console.log(a + " cells drawn, elapsed time = " + (Date.now() - time));
};

AbstractFieldViewLayer.prototype.refresh = function() {
    this.renderByChunkIds(this.displayedChunkIds);
    this.display();
};


AbstractFieldViewLayer.prototype.absoluteMousePositionToRelative = function(mousePosition) {
    return {
        x: mousePosition.x - this.offset.x, y: mousePosition.y - this.offset.y
    };
};

AbstractFieldViewLayer.prototype.mouseMove = function(mousePosition) {
    this.doOnMouseMove(this.absoluteMousePositionToRelative(mousePosition));

    // console.log(
    //     'mousePosition = ' + JSON.stringify(mousePosition) + ', offset = ' + JSON.stringify(this.offset)
    //     + ', result position = ' + JSON.stringify(layerMousePosition)
    // );
};

AbstractFieldViewLayer.prototype.doOnMouseMove = function(layerMousePosition) {
    //
};

// AbstractFieldViewLayer.prototype.drawByPositions = function(positions) {
//     //
// };