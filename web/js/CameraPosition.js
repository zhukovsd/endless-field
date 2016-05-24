/**
 * Created by ZhukovSD on 24.05.2016.
 */

var CameraPosition = function(originChunkId, shiftX, shiftY) {
    this.originChunkId = originChunkId;
    this.shift = {x: shiftX, y: shiftY};

    this.chunkOrigin = null;
};

CameraPosition.prototype = {
    getChunkOrigin: function() {
        if (this.chunkOrigin == null)
            this.chunkOrigin = ChunkIdGenerator.chunkOrigin(
                fieldView.fieldManager.chunkSize, fieldView.fieldManager.chunkIdFactor, this.originChunkId
            );

        return this.chunkOrigin;
    },

    clone: function() {
        return new CameraPosition(this.originChunkId, this.shift.x, this.shift.y);
    }, 

    shiftBy: function (mouseOffset, chunkSize, chunkIdFactor, cellSize) {
        var chunkWidthInPixels = chunkSize.columnCount * cellSize.width;
        var chunkHeightInPixels = chunkSize.rowCount * cellSize.height;
        
        var originChunkId = this.originChunkId;
        var x = this.shift.x - mouseOffset.x;
        var y = this.shift.y;
        
        if (x > chunkWidthInPixels) {
            originChunkId += Math.floor(x / chunkWidthInPixels);
            x = x % chunkWidthInPixels;
        } else if (x < 0) {

        }
        
        return new CameraPosition(originChunkId, x, y);
    }
};