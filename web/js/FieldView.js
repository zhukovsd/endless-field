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

var FieldView = function(fieldManager, containerId, drawSettings) {
    this.fieldManager = fieldManager;
    this.drawSettings = drawSettings;
    this.canvasContainer = null;
    this.camera = new Camera(this);

    this.layers = {};

    var view = this;
    window.addEventListener('load',
        function(event) {
            view.canvasContainer = document.getElementById(containerId);
            
            view.forEachLayer(function(layer) { layer.setSize(view.canvasContainer.clientWidth, view.canvasContainer.clientHeight); });
        }, false
    );

    this.addLayer = function(name, layer) {
        this.layers[name] = layer;
    };

    this.getLayer = function(name) {
        return this.layers[name];
    };

    this.forEachLayer = function(callback) {
        for (var name in this.layers) {
            if (this.layers.hasOwnProperty(name)) {
                callback(this.layers[name]);
            }
        }
    }
};