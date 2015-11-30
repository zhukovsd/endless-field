FieldView = function() {
    var canvasContainer = null;
    var canvas = null;
    var canvasContext = null;

    var width = 0;
    var height = 0;

    this.cameraPosition = {x: 0, y: 0};
    this.getSize = function() {
        return {width: canvas.style.width, height: canvas.style.height};
    };

    this.cameraScope = function() {
        var size = this.getSize();

        var x = Math.ceil((this.cameraPosition.x - 5) / 25) - 1;
        if ((this.cameraPosition.x - 5) % 25 > 21) x++;
        if ((this.cameraPosition.x - 5) % 25 == 0) x++;
        if (x < 0) x = 0;



        console.log(x);

        return {originRow: 0, originColumn: 0, rowCount: 0, columnCount: 0};
    };

    this.getCellsScope = function() {

    };

    this.init = function() {
        canvasContainer = document.getElementById('fieldcanvascontainer');
        canvas = document.getElementById('fieldcanvas');
        canvasContext = canvas.getContext('2d');

        setSize(canvas.clientWidth, canvas.clientHeight);

        var view = this;

        var isDragging = false;
        var dragMousePos = {};

        var cameraPositionOnMouseDown = {x: 0, y: 0};
        var mouseOffset = {x: 0, y: 0};

        function getMousePos(canvas, evt) {
            var rect = canvas.getBoundingClientRect();
            return {
                x: evt.clientX - rect.left,
                y: evt.clientY - rect.top
            };
        }

        canvas.addEventListener('click', function(evt) {
            //console.log('click');
            var mousePos = getMousePos(canvas, evt);
            fieldManager.cellClick(cellByPoint(mousePos));
        }, false);

        canvas.addEventListener('mousedown', function(evt) {
            //console.log('down');
            isDragging = true;
            dragMousePos = getMousePos(canvas, evt);
            cameraPositionOnMouseDown = {x: view.cameraPosition.x, y: view.cameraPosition.y};
        }, false);

        canvas.addEventListener('mousemove', function(evt) {
            //console.log('move');

            if (isDragging) {
                var mousePos = getMousePos(canvas, evt);

                mouseOffset = {x: mousePos.x - dragMousePos.x, y: mousePos.y - dragMousePos.y};
                view.cameraPosition = {x: cameraPositionOnMouseDown.x - mouseOffset.x, y: cameraPositionOnMouseDown.y - mouseOffset.y};

                if (view.cameraPosition.x < 0) view.cameraPosition.x = 0;
                if (view.cameraPosition.y < 0) view.cameraPosition.y = 0;

                //console.log(JSON.stringify(view.cameraPosition));
                view.cameraScope();

                view.paint();
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
        // setSize(700, 10 * 25 + 10);
        canvasContext.clearRect(0, 0, canvas.width, canvas.height);

        canvasContext.font = '7pt Arial';
        //canvasContext.fillStyle = 'black';
        //canvasContext.fillText('hi', 500, 25);

        canvasContext.fillStyle = "#cbcbcb";
        canvasContext.strokeStyle = 'black';
        canvasContext.lineWidth = 2;

        for (var row = 0; row < 10; row++) {
            for (var column = 0; column < 10; column++) {
                var cell = fieldManager.getCell(row, column);

                canvasContext.beginPath();
                canvasContext.rect(5 + column * 25 - this.cameraPosition.x, 5 + row * 25 - this.cameraPosition.y, 21, 21);

                if (cell.isChecked) {
                    canvasContext.fillStyle = "#cbcbcb";
                    canvasContext.fill();
                }

                canvasContext.fillStyle = "black";
                canvasContext.fillText(cell.text, 7 + column * 25 - this.cameraPosition.x, 14 + row * 25 - this.cameraPosition.y);

                canvasContext.stroke();
            }
        }
    }
};