/**
 * Created by ZhukovSD on 20.05.2016.
 */

var DrawSettings = function (cellWidth, cellHeight) {
    this.cellSize = {width: cellWidth, height: cellHeight};
};

var FieldView = function(fieldManager, drawSettings) {
    this.fieldManager = fieldManager;
    this.drawSettings = drawSettings;

    this.canvasContainer = null;
    this.canvas = null;
    this.canvasContext = null;

    // this.paintAreaWidth

    this.init = function(containerId, canvasId) {
        this.canvasContainer = document.getElementById(containerId);
        this.canvas = document.getElementById(canvasId);
        this.canvasContext = this.canvas.getContext('2d');
        
        this.canvas.width = this.canvas.clientWidth;
        this.canvas.height = this.canvas.clientHeight;

        // this.canvasContext.fillStyle = "#cbcbcb";
        // this.canvasContext.strokeStyle = 'black';
        // this.canvasContext.lineWidth = 2;
        //
        // this.canvasContext.beginPath();
        // this.canvasContext.rect(5, 5, 25, 25);
        // this.canvasContext.stroke();
    };

    this.cameraScope = function() {
        return new Scope(this.canvas.clientWidth, this.canvas.clientHeight, {x: 0, y: 0}, this.drawSettings.cellSize);
    };

    this.cellRect = function(row, column) {
        return {
            x: column * drawSettings.cellSize.width,
            y: row * drawSettings.cellSize.height,
            width: drawSettings.cellSize.width,
            height: drawSettings.cellSize.height
        };
    };

    //

    this.drawCellsByChunkIds = function(chunkIds) {
        var fieldManager = this.fieldManager;
        var view = this;

        //this.canvasContext.fillstyle = "black";
        this.canvasContext.strokeStyle = "black";
        this.canvasContext.font = "6pt Arial";
        this.canvasContext.lineWidth = 1;

        // fieldManager.chunkSize = {rowCount: 1, columnCount: 2};
        
        chunkIds.forEach(function(chunkId) {
            var origin = ChunkIdGenerator.chunkOrigin(fieldManager.chunkSize, fieldManager.chunkIdFactor, chunkId);

            for (var row = 0; row < fieldManager.chunkSize.rowCount; row++) {
                for (var column = 0; column < fieldManager.chunkSize.columnCount; column++) {
                    view.drawCell(
                        view.cellRect(origin.row + row, origin.column + column),
                        fieldManager.getCell(origin.row + row, origin.column + column)
                    );
                }
            }
        });
    };
};

FieldView.prototype = {
    drawCell: function(rect, cell) {
        var c = this.canvasContext;

        c.beginPath();
        c.rect(rect.x + 0.5, rect.y + 0.5, rect.width, rect.height);
        c.stroke();
        c.fillText(cell.text, rect.x + 2, rect.y + 9);
    }
};