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
 * Created by ZhukovSD on 22.09.2016.
 */

var OpenCellLayerAnimation = function() {
    AbstractLayerAnimation.call(this, 500);

    // this.alphaTransition = new FloatValueTransition(this.maxPosition, 1, 0);
    this.colorTransition = new ColorValueTransition('#ff0000', '#0000ff', 1, 0, this.maxPosition);
};

OpenCellLayerAnimation.prototype = Object.create(AbstractLayerAnimation.prototype);

OpenCellLayerAnimation.prototype.rect = function(cellPosition, chunksScope) {
    // TODO fix this
    var rect = chunksScope.cellRect(cellPosition);
    rect.x += 0.5;
    rect.y += 0.5;
    rect.width--;
    rect.height--;

    return rect;
};

OpenCellLayerAnimation.prototype.render = function(context, rect) {
    context.clearRect(rect.x, rect.y, rect.width, rect.height);

    context.save();
    // context.fillStyle = 'rgba(255, 0, 0, ' + alpha + ')';
    // context.fillStyle = 'rgba(255, 0, 0, ' + this.alphaTransition.currentValue(this.position) + ')';
    context.fillStyle = this.colorTransition.currentValue(this.position);
    context.fillRect(rect.x, rect.y, rect.width, rect.height);
    context.restore();
};
