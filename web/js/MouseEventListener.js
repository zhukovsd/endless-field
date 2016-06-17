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

MouseButton = {
    LEFT: 1,
    WHEEL: 2,
    RIGHT: 3
};

var MouseEventListener = function(fieldView, fieldViewTopLayerName) {
    this.fieldManager = fieldView.fieldManager;
    this.layer = null;
    
    var listener = this;

    var isDragging = false;
    var dragMouseButton = null;
    var mouseDownPos = null;
    var mouseDownCameraPosition = null;
    var mouseDownCameraScope = null;
    var handleClick = false;

    window.addEventListener('load',
        function(event) {
            listener.layer = fieldView.getLayer(fieldViewTopLayerName);

            listener.layer.canvas.addEventListener('mousedown', listener.mouseDownEvent, false);
            // canvas.addEventListener('mousemove', this.mouseMoveEvent, false);
            document.getElementsByTagName('html').item(0).addEventListener('mousemove', listener.mouseMoveEvent, false);
            // canvas.addEventListener('mouseup', this.mouseUpEvent, false);
            document.getElementsByTagName('html').item(0).addEventListener('mouseup', listener.mouseUpEvent, false);
            // canvas.addEventListener('mouseleave', this.mouseLeaveEvent, false);
            listener.layer.canvas.oncontextmenu = listener.contextMenuEvent;
            listener.layer.canvas.addEventListener('click', listener.mouseClickEvent, false);            
        }, false
    );

    function getMousePos(canvas, event) {
        var rect = canvas.getBoundingClientRect();
        return {
            x: event.clientX - rect.left,
            y: event.clientY - rect.top
        };
    }

    this.mouseDownEvent = function(event) {
        // console.log('mouse down ' + event.which);

        isDragging = true;
        dragMouseButton = event.which;
        mouseDownPos = getMousePos(listener.layer.canvas, event);

        if (dragMouseButton == MouseButton.LEFT) {
            mouseDownCameraPosition = fieldView.camera.position.clone();
            mouseDownCameraScope = fieldView.camera.cellsScope();

            // this variable used to determine how scope is changing during dragging.
            // (mouse move scope - this scope) = newly appeared cells which has to be drawn. since cells, which was only
            // partially visible in the beginning of the dragging also has to be redrawn, exclude them from current scope
            // to force their redraw on every dragging mouse move event
            mouseDownCameraScope.removePartiallyVisibleCells();

            // imageData = canvasContext.getImageData(0, 0, canvas.width, canvas.height);
            fieldView.forEachLayer(function(layer) { layer.storeImageData(); });
        }

        handleClick = true;
    };

    this.mouseMoveEvent = function(event) {
        if (isDragging) {
            var mousePos = getMousePos(listener.layer.canvas, event);
            var mouseOffset = {x: mousePos.x - mouseDownPos.x, y: mousePos.y - mouseDownPos.y};

            if ((handleClick) && (Math.abs(mouseOffset.x) + Math.abs(mouseOffset.y) > 5)) {
                handleClick = false;
                // console.log('don\'t handle click');
            }
       
            if (dragMouseButton == MouseButton.LEFT) {
                // shift camera position considering field boundaries (camera can't be shifted to the left from most left chunk, for example)
                var shiftedCameraPosition = mouseDownCameraPosition.shiftBy(
                    mouseOffset, listener.fieldManager.chunkSize, listener.fieldManager.chunkIdFactor,
                    fieldView.drawSettings.cellSize
                );

                // final offset may differ from mouseOffset, because user may rich field boundary while scrolling the field
                var offset = mouseDownCameraPosition.calculateMouseOffset(
                    shiftedCameraPosition, listener.fieldManager.chunkSize, listener.fieldManager.chunkIdFactor,
                    fieldView.drawSettings.cellSize
                );

                // canvasContext.clearRect(0, 0, canvas.width, canvas.height);
                // canvasContext.putImageData(imageData, offset.x, offset.y);
                fieldView.forEachLayer(function(layer) { layer.restoreImageData(offset); });

                fieldView.camera.setPosition(shiftedCameraPosition);

                // draw cells, which was out of scope on mouse down (todo also redraw cells, which was only partly visible)
                var newScope = fieldView.camera.cellsScope();
                if (!newScope.equals(mouseDownCameraScope)) {
                    var difference = newScope.difference(mouseDownCameraScope);
                    
                    // fieldView.drawByPositions(newScope.difference(mouseDownCameraScope));
                    fieldView.forEachLayer(function(layer) { layer.drawByPositions(difference); });
                }
            }

            // console.log(
            //     JSON.stringify(mouseOffset) + ", " + JSON.stringify(shiftedCameraPosition) + ", " +
            //     JSON.stringify(offset)
            // );
        }
    };

    this.mouseUpEvent = function(event) {
        // console.log('mouse up');

        if (isDragging) {
            isDragging = false;
            listener.onDragEnd();
        }
    };

    this.onDragEnd = function() {
        if (dragMouseButton == MouseButton.LEFT) {
            var mouseDownChunkIds = mouseDownCameraScope.chunkIds(this.fieldManager.chunkSize, this.fieldManager.chunkIdFactor);
            var mouseUpChunkIds = fieldView.camera.cellsScope().chunkIds(this.fieldManager.chunkSize, this.fieldManager.chunkIdFactor);

            // plain array comparison, array items have to be ordered in the same way
            var chunksScopeChanged = !(
                (mouseDownChunkIds.length == mouseUpChunkIds.length)
                &&
                (mouseDownChunkIds.every(function (v, i) {
                    return v === mouseUpChunkIds[i]
                }))
            );

            if (chunksScopeChanged) {
                // todo request only difference
                this.fieldManager.requestChunks(mouseUpChunkIds);
            }
        }
    };

    // this.mouseLeaveEvent = function(event) {
    //     console.log('leave');
    //
    //     if (isDragging) {
    //         isDragging = false;
    //         listener.onDragEnd();
    //     }
    // };

    //

    this.contextMenuEvent = function() {
        listener.mouseClickEvent(event);

        // prevent default context menu from showing
        return false;
    };

    this.mouseClickEvent = function(event) {
        if (handleClick) {
            var mousePos = getMousePos(listener.layer.canvas, event);
            var cellPosition = fieldView.camera.cellPositionByPoint(mousePos);

            listener.cellClicked(event.which, cellPosition);
        }
    };
};

MouseEventListener.prototype = {
    cellClicked: function(mouseButton, cellPosition) {
        console.log('mouse click ' + mouseButton + " " + cellPosition.row + "," + cellPosition.column);
    }
};