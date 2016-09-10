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
 * Created by ZhukovSD on 06.06.2016.
 */

var SimpleCellsFieldViewLayer = function(fieldView, canvasId) {
    CellsFieldViewLayer.call(this, fieldView, canvasId);
};

SimpleCellsFieldViewLayer.prototype = Object.create(CellsFieldViewLayer.prototype);

var inheritedDrawCell = SimpleCellsFieldViewLayer.prototype.drawCell;
SimpleCellsFieldViewLayer.prototype.drawCell = function(rect, cell, clear) {
    var c = this.imageData.renderContext;

    if (cell != null) {
        // clear rect for this cell only if clear flag equals true,
        // overrider drawCell() method may draw background for cell and then call this base method
        if (clear)
            c.clearRect(rect.x, rect.y, rect.width, rect.height);        
        
        if (cell.isChecked) {
            c.save();
            c.beginPath();
            c.rect(rect.x, rect.y, rect.width, rect.height);
            c.fillStyle = "#E8E8E8";
            c.fill();
            c.restore();            
        }
        
        inheritedDrawCell.call(this, rect, cell, false);
    }
};