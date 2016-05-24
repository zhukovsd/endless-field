/**
 * Created by ZhukovSD on 23.05.2016.
 */

var Camera = function(fieldView) {
    this.fieldView = fieldView;

    this.position = new CameraPosition(1, 0, 0);

    this.cellsScope = function() {
        var view = this.fieldView;
        var canvas = view.canvas;
        
        return new Scope(
            canvas.clientWidth, canvas.clientHeight, this.position, view.drawSettings.cellSize,
            view.fieldManager.chunkSize, view.fieldManager.chunkIdFactor
        );
    };

    this.cellRect = function(row, column) {
        var chunkOrigin = this.position.getChunkOrigin();
        var cellSize = this.fieldView.drawSettings.cellSize;

        return {
            // since canvas calculates from the half of a pixel, add 0.5 to prevent anti-aliasing
            y: (row - chunkOrigin.row) * cellSize.height - this.position.shift.y + 0.5,
            x: (column - chunkOrigin.column) * cellSize.width - this.position.shift.x + 0.5,
            width: cellSize.width,
            height: cellSize.height
        };
    };
};