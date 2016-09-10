/*
 * Copyright 2016 Zhukov Sergei
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        var chunkRow = Math.floor(originChunkId / chunkIdFactor);
        var chunkColumn = originChunkId % chunkIdFactor;

        // camera shifted beyond the right boundary of chunk
        if (x > chunkWidthInPixels) {
            //todo check for right field boundary (constraints)
            // originChunkId += Math.floor(x / chunkWidthInPixels);
            chunkColumn += Math.floor(x / chunkWidthInPixels);
            x %= chunkWidthInPixels;
        // camera shifted beyond the left boundary of chunk
        } else if (x < 0) {
            // originChunkId -= Math.ceil(Math.abs(x) / chunkWidthInPixels);
            chunkColumn -= Math.ceil(Math.abs(x) / chunkWidthInPixels);
            if (chunkColumn < 0) {
                chunkColumn = 0;
                x = 0;
            } else {
                x = chunkWidthInPixels + (x % chunkWidthInPixels);
            }
        }

        // camera shifted beyond the bottom boundary of chunk
        if (y > chunkHeightInPixels) {
            // todo check for bottom field boundary (constraints)
            chunkRow += Math.floor(y / chunkHeightInPixels);
            y %= chunkHeightInPixels;
        // camera shifted beyond the top boundary of chunk
        } else if (y < 0) {
            chunkRow -= Math.ceil(Math.abs(y) / chunkHeightInPixels);
            if (chunkRow < 0) {
                chunkRow = 0;
                y = 0;
            } else {
                y = chunkHeightInPixels + (y % chunkHeightInPixels);
            }
        }

        originChunkId = (chunkRow * chunkIdFactor) + chunkColumn;

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