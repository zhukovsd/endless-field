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

var AbstractLayerAnimation = function(duration) {
    this.position = 0;
    this.maxPosition = 100;

    this.id = animationCounter++;
    
    // animation duration in milliseconds
    this.duration = duration;
    this.startTimestamp = 0;
    this.removeOnNextFrame = false;

    this.start = function() {
        this.startTimestamp = Date.now();
    };
};

var animationCounter = 0;

AbstractLayerAnimation.prototype.updatePosition = function() {
    var elapsedTime = Date.now() - this.startTimestamp;
    var elapsedPosition = Math.floor((elapsedTime / this.duration) * this.maxPosition);

    // console.log('inherited, duration = ' + this.duration + ', maxPosition = ' + this.maxPosition);
    this.position = Math.min(elapsedPosition, this.maxPosition);
};

AbstractLayerAnimation.prototype.finished = function() {
    return (this.position == this.maxPosition);
};

AbstractLayerAnimation.prototype.rect = function(cellPosition, chunksScope) {
    // abstract rectByCellPosition
};

AbstractLayerAnimation.prototype.render = function(context, rect) {
    // abstract render
};