/**
 * Created by ZhukovSD on 16.05.2016.
 */

var SimpleFieldManager = function() {
    FieldManager.call(this);
};
SimpleFieldManager.prototype = Object.create(SimpleFieldManager.prototype);

SimpleFieldManager.prototype = {
    processResponseCells: function(chunkOrigin, responseCells) {
        alert("derived");
    }    
};