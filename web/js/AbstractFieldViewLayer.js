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
    
    var imageData = null;

    var layer = this;
    window.addEventListener('load',
        function(event) {
            layer.canvas = document.getElementById(canvasId);
            layer.context = layer.canvas.getContext('2d');
            
            layer.applySize();
        }, false
    );

    this.applySize = function() {
        if (this.canvas !== null) {
            this.canvas.width = this.width;
            this.canvas.height = this.height;
        }
    };
    
    this.setSize = function(width, height) {
        this.width = width;
        this.height = height;
        
        this.applySize();
    };

    this.clear = function() {
        if (this.context !== null) {
            this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        }
    };

    this.storeImageData = function() {
        if (this.context !== null) {
            imageData = this.context.getImageData(0, 0, this.canvas.width, this.canvas.height);
        }
    };

    this.restoreImageData = function(offset) {
        if (imageData !== null) {
            this.clear();
            this.context.putImageData(imageData, offset.x, offset.y);
        }
    }
};

AbstractFieldViewLayer.prototype.drawByPositions = function(positions) {
    //
};