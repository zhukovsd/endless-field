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
 * Created by ZhukovSD on 24.05.2016.
 */

var EventListener = function(fieldView) {
    var fieldManager = fieldView.fieldManager;

    var canvas = null;
    var canvasContext = null;

    var isDragging = false;
    var mouseDownPos = null;
    var mouseDownCameraPosition = null;
    var mouseDownCameraScope = null;
    var imageData = null;

    this.init = function(canvasId) {
        canvas = document.getElementById(canvasId);
        canvasContext = canvas.getContext('2d');
        
        canvas.addEventListener('mousedown', this.mouseDownEvent, false);
        canvas.addEventListener('mousemove', this.mouseMoveEvent, false);
        canvas.addEventListener('mouseup', this.mouseUpEvent, false);
    };

    function getMousePos(canvas, event) {
        var rect = canvas.getBoundingClientRect();
        return {
            x: event.clientX - rect.left,
            y: event.clientY - rect.top
        };
    }

    this.mouseDownEvent = function(event) {
        console.log('mouse down');

        isDragging = true;
        mouseDownPos = getMousePos(canvas, event);
        mouseDownCameraPosition = fieldView.camera.position.clone();
        mouseDownCameraScope = fieldView.camera.cellsScope();
        // this variable used to determine how scope is changing during dragging.
        // (mouse move scope - this scope) = newly appeared cells which has to be drawn. since cells, which was only
        // partially visible in the beginning of the dragging also has to be redrawn, exclude them from current scope
        // to force their redraw on every dragging mouse move event
        mouseDownCameraScope.removePartiallyVisibleCells();
        
        // reducedMouseDownCameraScope = mouseDownCameraScope.reduce(1);
        imageData = canvasContext.getImageData(0, 0, canvas.width, canvas.height);
    };

    this.mouseMoveEvent = function(event) {
        if (isDragging) {
            var mousePos = getMousePos(canvas, event);
            var mouseOffset = {x: mousePos.x - mouseDownPos.x, y: mousePos.y - mouseDownPos.y};

            // shift camera position considering field boundaries (camera can't be shifted to the left from most left chunk, for example)
            var shiftedCameraPosition = mouseDownCameraPosition.shiftBy(
                mouseOffset, fieldManager.chunkSize, fieldManager.chunkIdFactor,
                fieldView.drawSettings.cellSize
            );

            // final offset may differ from mouseOffset, because user may rich field boundary while scrolling the field
            var offset = mouseDownCameraPosition.calculateMouseOffset(
                shiftedCameraPosition, fieldManager.chunkSize, fieldManager.chunkIdFactor, fieldView.drawSettings.cellSize
            );

            canvasContext.clearRect(0, 0, canvas.width, canvas.height);
            canvasContext.putImageData(imageData, offset.x, offset.y);

            fieldView.camera.setPosition(shiftedCameraPosition);

            // draw cells, which was out of scope on mouse down (todo also redraw cells, which was only partly visible)
            var newScope = fieldView.camera.cellsScope();
            if (!newScope.equals(mouseDownCameraScope)) {
                fieldView.drawCellsByPositions(newScope.difference(mouseDownCameraScope));
            }

            // console.log(
            //     JSON.stringify(mouseOffset) + ", " + JSON.stringify(shiftedCameraPosition) + ", " +
            //     JSON.stringify(offset)
            // );
        }
    };
    
    this.mouseUpEvent = function() {
        console.log('mouse up');
        
        if (isDragging) {
            isDragging = false;

            var mouseDownChunkIds = mouseDownCameraScope.chunkIds(fieldManager.chunkSize, fieldManager.chunkIdFactor);
            var mouseUpChunkIds = fieldView.camera.cellsScope().chunkIds(fieldManager.chunkSize, fieldManager.chunkIdFactor);

            // plain array comparison, array items have to be ordered in the same way
            var chunksScopeChanged = !(
                (mouseDownChunkIds.length==mouseUpChunkIds.length)
                    &&
                (mouseDownChunkIds.every(function(v,i) { return v === mouseUpChunkIds[i]}))
            );

            if (chunksScopeChanged) {
                // todo request only difference
                fieldManager.requestChunks(mouseUpChunkIds);
            }
        }
    };
};