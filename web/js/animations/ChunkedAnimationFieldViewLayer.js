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
        animation.start();

        requestAnimationFrame(animationFrameCallback);
    };

    this.animationCount = function() {
        return Object.keys(this.animations).length;
    };

    var layer = this;
    var animationFrameCallback = function() {
        layer.refresh();

        if (layer.animationCount() > 0) {
            // console.log('request another frame');
            requestAnimationFrame(animationFrameCallback);
        } else {
            // console.log('all animations are over');
        }

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
    var animation = this.animations[key];

    animation.updateValue();    
    animation.render(this.imageData.renderContext, rect);

    if (animation.finished()) {
        delete this.animations[key];
    }
};

ChunkedAnimationFieldViewLayer.prototype.refresh = function() {
    // TODO fix this after migrating 'animations' to map
    var keys = Object.keys(this.animations);
    var positions = {};

    for (var i = 0; i < keys.length; i++) {
        var key = keys[i];
        var position = new CellPosition().fromKey(key);
        positions[position.toString()] = position;
    }

    // console.log('positions = ' + JSON.stringify(positions));

    // console.log('before imageData.width = ' + this.imageData.width + ', height = ' + this.imageData.height);

    // if (this.imageData.width == 0) {
    //     console.log('0');
    // }

    // var chunksScope = this.fieldView.currentChunksScope();
    // this.calculateImageDataSizeAndShift(chunksScope);
    // console.log('after imageData.width = ' + this.imageData.width + ', height = ' + this.imageData.height);

    this.renderByPositions(positions);
    this.display();
};