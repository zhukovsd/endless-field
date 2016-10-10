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
 * Created by ZhukovSD on 06.10.2016.
 */

var SimpleFieldCheckCellTextAnimation = function(count) {
    AbstractLayerAnimation.call(this, 400);
    
    this.count = count;

    this.yTransition = new FloatValueTransition(this.maxPosition, 0, 15);
    this.strokeColorTransition = new ColorValueTransition('#000000', '#000000', 1, 0.5, this.maxPosition);

    if (count > 0)
        this.fillColorTransition = new ColorValueTransition('#00ff00', '#00ff00', 1, 0.5, this.maxPosition);
    else
        this.fillColorTransition = new ColorValueTransition('#ff0000', '#ff0000', 1, 0.5, this.maxPosition);
};

SimpleFieldCheckCellTextAnimation.height = 50;

SimpleFieldCheckCellTextAnimation.prototype = Object.create(AbstractLayerAnimation.prototype);

SimpleFieldCheckCellTextAnimation.prototype.rect = function(cellPosition, chunksScope) {
    var cellRect = chunksScope.cellRect(cellPosition);
    
    return {x: cellRect.x - 0.5, y: cellRect.y, width: 50, height: SimpleFieldCheckCellTextAnimation.height};
};

SimpleFieldCheckCellTextAnimation.prototype.render = function(context, rect) {
    // context.save();

    // context.fillRect(rect.x, rect.y, rect.width, rect.height);
    // context.restore();

    // var z = rect.y + rect.height - context.measureText('test').height;
    // console.log('z = ' + z);

    context.font = "bold 20px Arial";
    context.fillStyle = this.fillColorTransition.currentValue(this.position);

    // console.log('id = ' + this.id + ', ' + context.fillStyle);

    context.strokeStyle = this.strokeColorTransition.currentValue(this.position);
    context.lineWidth = 1;

    var x = rect.x + 2;
    var y = rect.y + rect.height - 5 - this.yTransition.currentValue(this.position);

    var text = Math.abs(this.count) + ' cells';
    context.fillText(text, x, y);
    context.strokeText(text, x, y);
};
