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
 * Created by ZhukovSD on 05.06.2016.
 */

SimpleFieldActionType = {
    TOGGLE_CELL: 0,
    TOGGLE_SQUARE_REGION: 1,
    TOGGLE_ROUND_REGION: 2,

    selectByMouseButton: function(mouseButton) {
        switch(mouseButton) {
            case MouseButton.LEFT: return SimpleFieldActionType.TOGGLE_CELL;
            case MouseButton.RIGHT: return SimpleFieldActionType.TOGGLE_SQUARE_REGION;
            case MouseButton.WHEEL: return SimpleFieldActionType.TOGGLE_ROUND_REGION;
        }
    }
};

var SimpleMouseEventListener = function(fieldView, fieldViewTopLayerName) {
    MouseEventListener.call(this, fieldView, fieldViewTopLayerName);
};
SimpleMouseEventListener.prototype = Object.create(MouseEventListener.prototype);

SimpleMouseEventListener.prototype.cellClicked = function(mouseButton, cellPosition) {
    this.fieldManager.sendMessage(
        new ActionMessage(cellPosition, SimpleFieldActionType.selectByMouseButton(mouseButton))
    );
};