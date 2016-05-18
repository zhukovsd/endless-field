/**
 * Created by ZhukovSD on 16.05.2016.
 */

SimpleFieldManager = function() {
    FieldManager.prototype.onRequestResult = function(response) {alert("4312");};

    // FieldManager.onRequestResult(null);
    // console.log(">");
    FieldManager.prototype.onRequestResult = function(response) {alert("4312");};
    // FieldManager.onRequestResult(null);
    // console.log("<");
    
    // this.onRequestResult = function (response) {
    //     alert("sup");
    // }
};

SimpleFieldManager.prototype = new FieldManager();