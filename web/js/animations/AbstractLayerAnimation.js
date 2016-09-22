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

var AbstractLayerAnimation = function() {
    this.value = 0;
    this.duration = 0;
    this.startTimestamp = 0;
};

AbstractLayerAnimation.prototype.rect = function(cellPosition, chunksScope) {
    // abstract rectByCellPosition
};

AbstractLayerAnimation.prototype.render = function(context, rect) {
    // abstract render
};