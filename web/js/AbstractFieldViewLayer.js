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
    var imageData = null;
    var offset = {x: 0, y: 0}, storedOffset;

    var layer = this;
    window.addEventListener('load',
        function(event) {
            layer.canvas = document.getElementById(canvasId);
            layer.context = layer.canvas.getContext('2d');

            // todo: image data size factors as params
            imageData = new FieldViewLayerImageData(layer.canvas, 1.5, 1.5);
            layer.applyCanvasSize();

            offset = {x: -(layer.width * 0.25), y: -(layer.height * 0.25)};

            //
            var cn = document.createElement('canvas');
            cn.width = imageData.width;
            cn.height = imageData.height;
            var img = document.getElementById('test-img');
            var ct = cn.getContext('2d');

            ct.rect(0, 0, cn.width, cn.height);
            ct.lineWidth = 3;
            ct.stroke();
            
            ct.drawImage(img, 750, 300);
            imageData.data = ct.getImageData(0, 0, cn.width, cn.height);
            //
        }, false
    );

    this.applyCanvasSize = function() {
        if (this.canvas !== null) {
            this.canvas.width = this.width;
            this.canvas.height = this.height;
            
            imageData.updateSize(this.width, this.height);
        }
    };

    this.setCanvasSize = function(width, height) {
        this.width = width;
        this.height = height;

        this.applyCanvasSize();
    };

    this.clear = function() {
        if (this.context !== null) {
            this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        }
    };

    this.render = function() {
        this.clear();
        this.context.putImageData(imageData.data, offset.x, offset.y);
    };

    this.storeOffset = function() {
        storedOffset = { x: offset.x, y: offset.y };
    };

    this.shiftOffset = function(offsetDelta) {
        offset.x = storedOffset.x + offsetDelta.x;
        offset.y = storedOffset.y + offsetDelta.y;

        this.render();
    };

    // this.storeImageData = function() {
    //     if (this.context !== null) {
    //         imageData = this.context.getImageData(0, 0, this.canvas.width, this.canvas.height);
    //     }
    // };
    //
    // this.restoreImageData = function(offset) {
    //     if (imageData !== null) {
    //         this.clear();
    //         this.context.putImageData(imageData, offset.x, offset.y);
    //     }
    // }
};

// AbstractFieldViewLayer.prototype.drawByPositions = function(positions) {
//     //
// };