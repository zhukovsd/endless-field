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
        imageData = canvasContext.getImageData(0, 0, canvas.width, canvas.height);
    };

    this.mouseMoveEvent = function(event) {
        if (isDragging) {
            var mousePos = getMousePos(canvas, event);
            var mouseOffset = {x: mousePos.x - mouseDownPos.x, y: mousePos.y - mouseDownPos.y};

            var shiftedCameraPosition = mouseDownCameraPosition.shiftBy(
                mouseOffset, fieldManager.chunkSize, fieldManager.chunkIdFactor,
                fieldView.drawSettings.cellSize
            );

            var offset = mouseDownCameraPosition.calculateMouseOffset(
                shiftedCameraPosition, fieldManager.chunkSize, fieldManager.chunkIdFactor, fieldView.drawSettings.cellSize
            );

            canvasContext.clearRect(0, 0, canvas.width, canvas.height);
            canvasContext.putImageData(imageData, mouseOffset.x, mouseOffset.y);

            console.log(
                JSON.stringify(mouseOffset) + ", " + JSON.stringify(shiftedCameraPosition) + ", " +
                JSON.stringify(offset)
            );
        }
    };
    
    this.mouseUpEvent = function() {
        console.log('mouse up');
        
        if (isDragging) {
            isDragging = false;
            //
        }
    };
};