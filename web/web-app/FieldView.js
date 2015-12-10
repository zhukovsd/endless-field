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

        // TODO: 08.12.2015 remove hardcoded params
        var leftVisibleColumnIndex = Math.ceil((this.cameraPosition.x - 5) / 25) - 1;
        // if mod > cell width, then this cell are not visible yet
        if ((this.cameraPosition.x - 5) % 25 > 21) leftVisibleColumnIndex++;
        if ((this.cameraPosition.x - 5) % 25 == 0) leftVisibleColumnIndex++;
        if (leftVisibleColumnIndex < 0) leftVisibleColumnIndex = 0;

        var topVisibleRowIndex = Math.ceil((this.cameraPosition.y - 5) / 25) - 1;
        // if mod > cell height, then this cell are not visible yet
        if ((this.cameraPosition.y - 5) % 25 > 21) topVisibleRowIndex++;
        if ((this.cameraPosition.y - 5) % 25 == 0) topVisibleRowIndex++;
        if (topVisibleRowIndex < 0) topVisibleRowIndex = 0;

        var leftTopCellOrigin = {
            x: 5 + leftVisibleColumnIndex * 25 - this.cameraPosition.x,
            y: 5 + topVisibleRowIndex * 25 - this.cameraPosition.y
        };

        var visibleColumnsCount = Math.ceil((width - leftTopCellOrigin.x) / 25);
        if ((width - leftTopCellOrigin.x) % 25 == 0) visibleColumnsCount++;

        var visibleRowsCount = Math.ceil((height - leftTopCellOrigin.y) / 25);
        if ((height - leftTopCellOrigin.y) % 25 == 0) visibleRowsCount++;

        //console.log(leftVisibleColumnIndex + ", " + topVisibleRowIndex + ", " + visibleColumnsCount);

        return {
            originRow: topVisibleRowIndex, originColumn: leftVisibleColumnIndex,
            rowCount: visibleRowsCount, columnCount: visibleColumnsCount
        };
    };

    this.cellsScope = function() {
        var cameraScope = this.cameraScope();

        // TODO: 08.12.2015 cellsScope, cameraScope results as Rect objects
        var result = {
            originRow: cameraScope.originRow - Math.round(cameraScope.rowCount / 2),
            originColumn: cameraScope.originColumn - Math.round(cameraScope.columnCount / 2),
            rowCount: cameraScope.rowCount * 2,
            columnCount: cameraScope.columnCount * 2
        };

        if (result.originRow < 0) result.originRow = 0;
        if (result.originColumn < 0) result.originColumn = 0;

        return result;
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
        var a = {};

        //var dataURL;
        //var image;

        function doOnClick(evt) {
            //console.log('click');
            var mousePos = getMousePos(canvas, evt);
            fieldManager.cellClick(view.cellByPoint(mousePos));
        }

        function doOnDragEnd() {
            if ((a.originRow != view.cameraScope().originRow) || (a.originColumn != view.cameraScope().originColumn)) {
                fieldManager.requestField(fieldView.cellsScope());
            } else {
                //console.log('no');
            }
        }

        function getMousePos(canvas, evt) {
            var rect = canvas.getBoundingClientRect();
            return {
                x: evt.clientX - rect.left,
                y: evt.clientY - rect.top
            };
        }

        //

        canvas.addEventListener('mousedown', function(evt) {
            //console.log('down');

            evt.preventDefault();

            isDragging = true;
            dragMousePos = getMousePos(canvas, evt);
            cameraPositionOnMouseDown = {x: view.cameraPosition.x, y: view.cameraPosition.y};
            a = view.cameraScope();

            //dataURL = canvas.toDataURL();
            //image = new Image();
            //image.src = dataURL;

            //
            canvas.addEventListener('click', doOnClick, false);
        }, false);

        canvas.addEventListener('mousemove', function(evt) {
            //console.log('move');

            if (isDragging) {
                var mousePos = getMousePos(canvas, evt);

                mouseOffset = {x: mousePos.x - dragMousePos.x, y: mousePos.y - dragMousePos.y};
                view.cameraPosition = {x: cameraPositionOnMouseDown.x - mouseOffset.x, y: cameraPositionOnMouseDown.y - mouseOffset.y};

                if (view.cameraPosition.x < 0) view.cameraPosition.x = 0;
                if (view.cameraPosition.y < 0) view.cameraPosition.y = 0;

                if (Math.abs(mouseOffset.x) + Math.abs(mouseOffset.y) > 5) {
                    canvas.removeEventListener('click', doOnClick);
                    //console.log('event removed');
                }

                // console.log(JSON.stringify(view.cameraScope()));

                //canvasContext.clearRect(0, 0, canvas.width, canvas.height);
                //canvasContext.drawImage(image, mouseOffset.x, mouseOffset.y);

                // TODO optimize paint of mouse move
                view.paint();
            }
        }, false);

        canvas.addEventListener('mouseup', function(evt) {
            console.log('up');

            if (isDragging) {
                isDragging = false;
                doOnDragEnd();
            }
        }, false);

        canvas.addEventListener('mouseleave', function(evt) {
            //console.log('leave');
            if (isDragging) {
                isDragging = false;
                doOnDragEnd();
            }
        }, false);

        var i = 0;

        canvas.addEventListener('touchmove', function(evt) {
            canvasContext.fillText(i.toString(), 5 + i, 5);

            i += 15;
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

    this.cellByPoint = function(Point) {
        var cellPos = {};

        cellPos.row = Math.floor((this.cameraPosition.y + Point.y - 5) / 25);
        cellPos.column = Math.floor((this.cameraPosition.x + Point.x - 5) / 25);

        if (((Point.x - 5) % 25 > 21) || ((Point.y - 5) % 25 > 21))
            cellPos = null;

        return cellPos;
    };

    // TODO repaint only certain cell
    this.paint = function() {
        // setSize(700, 10 * 25 + 10);
        canvasContext.clearRect(0, 0, canvas.width, canvas.height);

        canvasContext.font = '6pt Arial';
        //canvasContext.fillStyle = 'black';
        //canvasContext.fillText('hi', 500, 25);

        canvasContext.fillStyle = "#cbcbcb";
        canvasContext.strokeStyle = 'black';
        canvasContext.lineWidth = 2;

        var cameraScope = this.cameraScope();
        for (var row = cameraScope.originRow; row < cameraScope.originRow + cameraScope.rowCount; row++) {
            for (var column = cameraScope.originColumn; column < cameraScope.originColumn + cameraScope.columnCount; column++) {
                var cell = fieldManager.getCell(row, column);

                canvasContext.beginPath();
                canvasContext.rect(5 + column * 25 - this.cameraPosition.x, 5 + row * 25 - this.cameraPosition.y, 21, 21);

                if (cell != null) {
                    if (cell.isChecked) {
                        canvasContext.fillStyle = "#cbcbcb";
                        canvasContext.fill();
                    }

                    canvasContext.fillStyle = "black";
                    canvasContext.fillText(cell.text, 7 + column * 25 - this.cameraPosition.x, 14 + row * 25 - this.cameraPosition.y);

                    canvasContext.stroke();
                } else {
                    canvasContext.fillStyle = "#ffaaaa";
                    canvasContext.fill();
                }
            }
        }
    }
};