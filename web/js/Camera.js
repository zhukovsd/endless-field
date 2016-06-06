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
 * Created by ZhukovSD on 23.05.2016.
 */

var Camera = function(fieldView) {
    this.fieldView = fieldView;

    this.position = new CameraPosition(1, 0, 0);

    this.onPositionChanged = null;
    
    this.setPosition = function(position) {
        this.position = position;
        
        if (this.onPositionChanged != null) {
            this.onPositionChanged(this.position);
        }
    };
    
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

    this.cellPositionByPoint = function(mousePosition) {
        var chunkOrigin = this.position.getChunkOrigin();
        var cellSize = this.fieldView.drawSettings.cellSize;

        return new CellPosition(
            Math.floor((mousePosition.y + this.position.shift.y) / cellSize.height) + chunkOrigin.row,
            Math.floor((mousePosition.x + this.position.shift.x) / cellSize.width) + chunkOrigin.column
        );
    }
};