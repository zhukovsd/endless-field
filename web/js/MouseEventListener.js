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

    this.mousePos = null;

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

            fieldView.forEachLayer(function(layer) { layer.storeOffset(); });
        }

        handleClick = true;
    };

    this.mouseMoveEvent = function(event) {
        var mousePos = getMousePos(listener.layer.canvas, event);
        listener.mousePos = mousePos;

        if (isDragging) {
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

                fieldView.forEachLayer(function(layer) {
                    layer.shiftOffset(offset);
                });

                fieldView.camera.setPosition(shiftedCameraPosition);
            }

            // console.log(
            //     JSON.stringify(mouseOffset) + ", " + JSON.stringify(shiftedCameraPosition) + ", " +
            //     JSON.stringify(offset)
            // );
        } else {
            fieldView.forEachLayer(function(layer) {
                layer.mouseMove(mousePos);
            });
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
            fieldView.updateExpandedScopeChunkIds();
        }
    };

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