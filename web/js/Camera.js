/**
 * Created by ZhukovSD on 23.05.2016.
 */

var Camera = function(fieldView) {
    this.fieldView = fieldView;

    this.position = {
        originChunkId: 1,
        shift: {
            x: 0, y: 0
        },

        // origin of chunk with id = this.originChunkId
        chunkOrigin: null,

        getChunkOrigin: function() {
            if (this.chunkOrigin == null)
                this.chunkOrigin = ChunkIdGenerator.chunkOrigin(
                    fieldView.fieldManager.chunkSize, fieldView.fieldManager.chunkIdFactor, this.originChunkId
                );

            return this.chunkOrigin;
        }
    };

    this.cellsScope = function() {
        return new Scope(
            this.fieldView.canvas.clientWidth, this.fieldView.canvas.clientHeight, this.position,
            this.fieldView.drawSettings.cellSize, this.fieldView.fieldManager.chunkSize, this.fieldView.fieldManager.chunkIdFactor
        );
    };

    this.cellRect = function(row, column) {
        var chunkOrigin = this.position.getChunkOrigin();
        var cellSize = this.fieldView.drawSettings.cellSize;

        return {
            // since canvas calculates from the half of a pixel, add 0.5 to prevent anti-aliasing
            y: (chunkOrigin.row - row) * cellSize.height - this.position.shift.y + 0.5,
            x: (chunkOrigin.column - column) * cellSize.width - this.position.shift.x + 0.5,
            width: cellSize.width,
            height: cellSize.height
        };
    };
};