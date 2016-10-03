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
    this.c = 0;
    
    // animation request ID
    var id;
    var stop;
    var frameCount = 0;
    var fps, fpsInterval, startTime, now, then, elapsed;

    fps = 30;

    this.addAnimation = function(cellPosition, animation) {
        this.c++;
        this.animations[cellPosition.toString()] = animation;
        animation.start();

        // requestAnimationFrame(animationFrameCallback);

        stop = false;
        fpsInterval = 1000 / fps;
        frameCount = 0;
        then = Date.now();
        startTime = then;
        
        animate();
    };

    this.animationCount = function() {
        // return Object.keys(this.animations).length;
        return this.c;
    };

    var layer = this;

    // var animationFrameCallback = function() {
    //     layer.refresh();
    //
    //     if (layer.animationCount() > 0) {
    //         // console.log('request another frame');
    //         requestAnimationFrame(animationFrameCallback);
    //     } else {
    //         // console.log('all animations are over');
    //     }
    // }

    // http://stackoverflow.com/questions/19764018/controlling-fps-with-requestanimationframe
    function animate() {
        if (!stop) {
            // request another frame
            id = requestAnimationFrame(animate);

            // calc elapsed time since last loop
            now = Date.now();
            elapsed = now - then;

            // if enough time has elapsed, draw the next frame
            if (elapsed > fpsInterval) {
                // Get ready for next frame by setting then=now, but also adjust for your
                // specified fpsInterval not being a multiple of RAF's interval (16.7ms)
                then = now - (elapsed % fpsInterval);

                // Put your drawing code here
                layer.refresh();

                var sinceStart = now - startTime;
                var currentFps = Math.round(1000 / (sinceStart / ++frameCount) * 100) / 100;

                // console.log('current fps = ' + currentFps);
            }

            if (layer.animationCount() == 0) {
                // alert('123');
                stop = true;
                console.log('stop');

                cancelAnimationFrame(id);
            }
        }
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

    animation.updatePosition();    
    animation.render(this.imageData.renderContext, rect);

    if (animation.finished()) {
        delete this.animations[key];
        this.c--;
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

    this.renderByPositions(positions);
    this.display();
    // this.test({x: 0, y: 0, width: 1100, height: 800});
};