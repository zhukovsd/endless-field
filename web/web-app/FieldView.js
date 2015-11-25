var FieldView = new function() {
    var canvasContainer = null;
    var canvas = null;
    var canvasContext = null;

    var width = 5;
    var height = 0;

    //var graphsData = new Array();

    this.init = function() {
        canvasContainer = document.getElementById('fieldcanvascontainer');
        canvas = document.getElementById('fieldcanvas');
        canvasContext = canvas.getContext('2d');
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

    this.paint = function() {
        setSize(700, 10 * 25 + 10);
        canvasContext.clearRect(0, 0, canvas.width, canvas.height);

        canvasContext.font = '18pt Calibri';
        canvasContext.fillStyle = 'black';
        canvasContext.fillText('hi', 500, 25);

        canvasContext.fillStyle = "#ebebeb";
        canvasContext.strokeStyle = 'black';
        canvasContext.lineWidth = 2;

        var cells = FieldManager.cells;
        for (var i = 0; i < cells.length; i++) {
            canvasContext.beginPath();
            canvasContext.rect(5 + cells[i].column * 25, 5 + cells[i].row * 25, 21, 21);
            canvasContext.stroke();

            if (cells[i].isChecked) {
                canvasContext.fill();
            }
        }
    }
};