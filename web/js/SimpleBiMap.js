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
 * Created by ZhukovSD on 09.08.2016.
 */

var SimpleBiMap = function() {
    var keys = {}, values = {}, kvMap = {}, vkMap = {};

    this.containsKey = function (key) {
        return keys.hasOwnProperty(key.toString());
    };

    this.containsValue = function(value) {
        return values.hasOwnProperty(value.toString());
    };

    this.put = function(key, value) {
        if (this.containsKey(key))
            this.removeKey(key);
        
        if (this.containsValue(value))
            this.removeValue(value);

        var keyString = key.toString();
        var valueString = value.toString();

        keys[keyString] = key;
        values[valueString] = value;

        kvMap[keyString] = valueString;
        vkMap[valueString] = keyString;
    };

    this.value = function(key) {
        if (this.containsKey(key)) {
            return values[kvMap[key.toString()]];
        }
    };

    this.key = function(value) {
        if (this.containsValue(value)) {
            return keys[vkMap[value.toString()]];
        }
    };

    var removePair = function(key, value) {
        var keyString = key.toString();
        var valueString = value.toString();

        delete keys[keyString];
        delete values[valueString];
        delete kvMap[keyString];
        delete vkMap[valueString];
    };

    this.removeKey = function(key) {
        if (this.containsKey(key)) {
            removePair(key, this.value(key));
        }
    };

    this.removeValue = function(value) {
        if (this.containsValue(value)) {
            removePair(this.key(value), value);
        }
    }
};