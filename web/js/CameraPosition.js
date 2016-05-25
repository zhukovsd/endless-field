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
        var y = this.shift.y - mouseOffset.y;

        // camera shifted beyond the right boundary of chunk
        if (x > chunkWidthInPixels) {
            originChunkId += Math.floor(x / chunkWidthInPixels);
            x %= chunkWidthInPixels;
        // camera shifted beyond the left boundary of chunk
        } else if (x < 0) {
            originChunkId -= Math.ceil(Math.abs(x) / chunkWidthInPixels);
            x = chunkWidthInPixels + (x % chunkWidthInPixels);
        }

        // camera shifted beyond the bottom boundary of chunk
        if (y > chunkHeightInPixels) {
            originChunkId += Math.floor(y / chunkHeightInPixels) * chunkIdFactor;
            y %= chunkHeightInPixels;
        // camera shifted beyond the top boundary of chunk
        } else if (y < 0) {
            originChunkId -= Math.ceil(Math.abs(y) / chunkHeightInPixels) * chunkIdFactor;
            y = chunkHeightInPixels + (y % chunkHeightInPixels);
        }
        
        return new CameraPosition(originChunkId, x, y);
    },

    calculateMouseOffset: function(position, chunkSize, chunkIdFactor, cellSize) {
        var chunkWidthInPixels = chunkSize.columnCount * cellSize.width;
        var chunkHeightInPixels = chunkSize.rowCount * cellSize.height;

        var hChunkOffset = (this.originChunkId % chunkIdFactor - position.originChunkId % chunkIdFactor) * chunkWidthInPixels;
        var hOffset = hChunkOffset + this.shift.x - position.shift.x;

        var vChunkOffset = (
            Math.floor(this.originChunkId / chunkIdFactor) - Math.floor(position.originChunkId / chunkIdFactor)
        ) * chunkHeightInPixels;
        var vOffset = vChunkOffset + this.shift.y - position.shift.y; 
        
        return {x: hOffset, y: vOffset};
    }
};