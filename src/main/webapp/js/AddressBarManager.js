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
 * Created by ZhukovSD on 29.05.2016.
 */

var AddressBarManager = function(pathPrefix) {
    var url = document.createElement('a');
    
    this.getChunkId = function() {
        var result = null;

        url.href = window.location;
        var path = url.pathname;

        var pos = path.indexOf(pathPrefix);
        
        if (pos != -1) {
           var subStr = path.substr(pos + pathPrefix.length, path.length);
        
           if (subStr != '') {
               // remove trailing slash
               subStr = subStr.replace(/\/$/, "");
        
               var chunkId = Number(subStr);
               if (!isNaN(chunkId)) {
                   result = chunkId;
               }
           }
        }
        
        return result;
    };

    var currentChunkId = this.getChunkId();

    this.setChunkId = function(chunkId) {
        if (currentChunkId != chunkId) {
            currentChunkId = chunkId;
        
            url.href = window.location;
            url.pathname = pathPrefix + currentChunkId;
        
            window.history.pushState(null, "", url.href);
        
            console.log("history length = " + window.history.length);
        }          
    };
};