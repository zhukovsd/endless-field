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

// TODO rename to SimpleFieldCheckCellAnimation
var OpenCellLayerAnimation = function(isReversed) {
    AbstractReversibleLayerAnimation.call(this, 200, isReversed);

    // var unchecked = '#000000';
    var unchecked = '#E8E8E8';
    var checked = '#FFFFFF';
    // var unchecked = '#FF0000';
    // var checked = '#0000FF';

    // unreversed -> from white to gray (from unchecked to checked)
    this.fillColorTransition = new ColorValueTransition(checked, unchecked, 1, 1, this.maxPosition);
};

OpenCellLayerAnimation.prototype = Object.create(AbstractReversibleLayerAnimation.prototype);

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
    context.save();
    context.fillStyle = this.fillColorTransition.currentValue(this.position);
    context.fillRect(rect.x, rect.y, rect.width, rect.height);
    context.restore();
};
