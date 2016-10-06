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
 * Created by ZhukovSD on 05.10.2016.
 */

var AbstractReversibleLayerAnimation = function(duration, isReversed) {
    AbstractLayerAnimation.call(this, duration);

    this.isReversed = isReversed;

    this.setIsReversed = function(value) {
        if (this.isReversed != value) {
            // recalculate start time to affect position calculation
            // if 30% of animation passed, on reversing it we need to 'set' position to 70% by changing its startTimestamp
            this.updatePosition();
            var currentPosition = this.position;
            var desiredPosition = this.maxPosition - this.position;

            this.startTimestamp = Date.now() - (desiredPosition * this.duration) / this.maxPosition;

            this.isReversed = value;
        }
    }
};

AbstractReversibleLayerAnimation.prototype = Object.create(AbstractLayerAnimation.prototype);

var inheritedUpdatePosition = AbstractReversibleLayerAnimation.prototype.updatePosition;
AbstractReversibleLayerAnimation.prototype.updatePosition = function() {
    inheritedUpdatePosition.call(this);

    if (this.isReversed) {
        this.position = this.maxPosition - this.position;
    }

    // console.log('position = ' + this.position);
};

var inheritedFinished = AbstractReversibleLayerAnimation.prototype.finished;
AbstractReversibleLayerAnimation.prototype.finished = function() {
    if (!this.isReversed)
        return inheritedFinished.call(this);
    else
        return (this.position == 0);
};