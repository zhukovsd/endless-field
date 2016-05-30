/**
 * Created by ZhukovSD on 29.05.2016.
 */

var URIManager = function(pathPrefix) {
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