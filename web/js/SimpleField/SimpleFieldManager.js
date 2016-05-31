/**
 * Created by ZhukovSD on 16.05.2016.
 */

var SimpleFieldManager = function(applicationContextPath) {
    FieldManager.call(this, applicationContextPath);
};
SimpleFieldManager.prototype = Object.create(FieldManager.prototype);

SimpleFieldManager.prototype.processResponseCell = function(cell) {
    return {
        isChecked: cell.hasOwnProperty("c")
    };
};