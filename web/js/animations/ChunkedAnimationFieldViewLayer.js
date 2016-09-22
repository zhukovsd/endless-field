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

var ChunkedAnimationFieldViewLayer = function (fieldView, canvasId) {
    AbstractFieldViewLayer.call(this, fieldView, canvasId);

    // key - position, value - animation
    // TODO map
    this.animations = {};

    this.addAnimation = function(cellPosition, animation) {
        this.animations[cellPosition.toString()] = animation;

        requestAnimationFrame(animationFrameCallback);
    };

    var layer = this;
    var animationFrameCallback = function(a) {
        console.log(a);

        // requestAnimationFrame(animationFrameCallback)
    }
};

ChunkedAnimationFieldViewLayer.prototype = Object.create(AbstractFieldViewLayer.prototype);

ChunkedAnimationFieldViewLayer.prototype.rectByPosition = function(position, chunksScope) {
    var key = position.toString();
    if (key in this.animations) {
        return this.animations[key].rect(position, chunksScope);
    }
};

ChunkedAnimationFieldViewLayer.prototype.renderByPosition = function(position, rect) {
    // render animation for given rect
    var key = position.toString();
    var animation = this.animations;

    

    animation[key].render(this.imageData.renderContext, rect);
};