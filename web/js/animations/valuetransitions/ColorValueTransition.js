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
 * Created by ZhukovSD on 03.10.2016.
 */

function hexToRgb(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : null;
}

var CONST = 'CONST';

var ColorValueTransition = function(startColor, stopColor, startAlpha, stopAlpha, maxPosition) {
    AbstractValueTransition.call(this, maxPosition);

    this.startRgb = hexToRgb(startColor);
    this.stopRgb = hexToRgb(stopColor);
    this.startAlpha = startAlpha;
    this.stopAlpha = stopAlpha;

    this.channelTransition = {
        r: (this.startRgb.r == this.stopRgb.r) ? CONST : new FloatValueTransition(maxPosition, this.startRgb.r, this.stopRgb.r),
        g: (this.startRgb.g == this.stopRgb.g) ? CONST : new FloatValueTransition(maxPosition, this.startRgb.g, this.stopRgb.g),
        b: (this.startRgb.b == this.stopRgb.b) ? CONST : new FloatValueTransition(maxPosition, this.startRgb.b, this.stopRgb.b),
        a: (this.startAlpha == this.stopAlpha) ? CONST : new FloatValueTransition(maxPosition, this.startAlpha, this.stopAlpha)
    };
};

ColorValueTransition.prototype = Object.create(AbstractValueTransition.prototype);

ColorValueTransition.prototype.currentValue = function(position) {
    var r = (this.channelTransition.r === CONST) ? this.startRgb.r : this.channelTransition.r.currentValue(position);
    var g = (this.channelTransition.g === CONST) ? this.startRgb.g : this.channelTransition.g.currentValue(position);
    var b = (this.channelTransition.b === CONST) ? this.startRgb.b : this.channelTransition.b.currentValue(position);
    var a = (this.channelTransition.a === CONST) ? this.startAlpha : this.channelTransition.a.currentValue(position);

    // console.log(result);
    return 'rgba(' + Math.round(r) + ',' + Math.round(g) + ',' + Math.round(b) + ',' + a + ')';
};