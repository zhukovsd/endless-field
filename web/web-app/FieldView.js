FieldView = function() {
    var canvasContainer = null;
    var canvas = null;
    var canvasContext = null;

    var width = 5;
    var height = 0;

    var isDragging = false;
    var dragMousePos = {};

    var offsetX = 0;
    var offsetY = 0;

    //var graphsData = new Array();

    this.init = function() {
        canvasContainer = document.getElementById('fieldcanvascontainer');
        canvas = document.getElementById('fieldcanvas');
        canvasContext = canvas.getContext('2d');

        var view = this;

        function getMousePos(canvas, evt) {
            var rect = canvas.getBoundingClientRect();
            return {
                x: evt.clientX - rect.left,
                y: evt.clientY - rect.top
            };
        }

        canvas.addEventListener('click', function(evt) {
            var mousePos = getMousePos(canvas, evt);
            fieldManager.cellClick(cellByPoint(mousePos));
        }, false);

        canvas.addEventListener('mousedown', function(evt) {
            // console.log('down');
            isDragging = true;
            dragMousePos = getMousePos(canvas, evt);
        }, false);

        canvas.addEventListener('mousemove', function(evt) {
            //console.log('move');

            if (isDragging) {
                var mousePos = getMousePos(canvas, evt);

                offsetX = mousePos.x - dragMousePos.x;
                offsetY = mousePos.y - dragMousePos.y;
                view.paint();

                // console.log(offsetX + ", " + offsetY);
            }
        }, false);

        canvas.addEventListener('mouseup', function(evt) {
            //console.log('up');
            isDragging = false;
        }, false);
    };

    function setSize(awidth, aheight) {
        if ((width != awidth) || (height != aheight)) {
            width = awidth;
            height = aheight;

            canvasContainer.style.height = height + "px";

            canvas.style.width = width+"px";
            canvas.style.height = (height)+"px";

            canvas.width = canvas.clientWidth;
            canvas.height = canvas.clientHeight;

            // if scrollbar is visible, then canvasContainer height = canvas height + scrollbar height
            if (canvasContainer.scrollHeight > canvasContainer.clientHeight)
                canvasContainer.style.height = (height + (canvasContainer.scrollHeight - canvasContainer.clientHeight)) + "px";
            else
                canvasContainer.style.height = height + "px";
        }
    }

    function cellByPoint(Point) {
        var cellPos = {};
        cellPos.row = Math.floor((Point.y - 5) / 25);
        cellPos.column = Math.floor((Point.x - 5) / 25);

        if (((Point.x - 5) % 25 > 21) | ((Point.y - 5) % 25 > 21))
            cellPos = null;

        return cellPos;
    }

    this.paint = function() {
        setSize(700, 10 * 25 + 10);
        canvasContext.clearRect(0, 0, canvas.width, canvas.height);

        canvasContext.font = '18pt Calibri';
        canvasContext.fillStyle = 'black';
        canvasContext.fillText('hi', 500, 25);

        canvasContext.fillStyle = "#cbcbcb";
        canvasContext.strokeStyle = 'black';
        canvasContext.lineWidth = 2;

        for (var row = 0; row < 10; row++) {
            for (var column = 0; column < 10; column++) {
                var cell = fieldManager.getCell(row, column);

                canvasContext.beginPath();
                canvasContext.rect(5 + column * 25 + offsetX, 5 + row * 25 + offsetY, 21, 21);

                if (cell.isChecked) {
                    canvasContext.fill();
                }

                canvasContext.stroke();
            }
        }
    }
};