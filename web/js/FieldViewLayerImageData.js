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
 * Created by ZhukovSD on 19.07.2016.
 */

var FieldViewLayerImageData = function(layer/*layerCanvas, widthFactor, heightFactor*/) {
    // var canvas = layerCanvas;

    this.renderCanvas = document.createElement('canvas');
    this.renderContext = this.renderCanvas.getContext('2d');

    // this.context = null;

    this.width = 0;
    this.height = 0;

    // this.data = null;

    this.setSize = function(width, height) {
        // var newWidth = width * widthFactor;
        // var newHeight = height * heightFactor;

        // if (this.width != newWidth || this.height != newHeight) {
        if (this.width != width || this.height || height) {
            this.width = width;
            this.height = height;

            this.renderCanvas.width = this.width;
            this.renderCanvas.height = this.height;

            layer.initRenderCanvasStyleSettings();

            //
            // var img = document.getElementById('test-img');
            // this.renderContext.rect(0, 0, this.width, this.height);
            // this.renderContext.lineWidth = 3;
            // this.renderContext.stroke();
            // this.renderContext.drawImage(img, 550, 300);
            //

            // todo copy existing image data on resize

            // this.data = renderContext.getImageData(0, 0, renderCanvas.width, renderCanvas.height);
            // }
        }
    }
};