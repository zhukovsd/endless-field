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

            fieldView.setCameraPosition(shiftedCameraPosition);

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
            //
        }
    };
};