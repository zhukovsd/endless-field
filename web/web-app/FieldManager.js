/**
 * Created by ZhukovSD on 25.11.2015.
 */

FieldManagerState = {
    UNINITIALIZED: 0,
    LOADING: 1,
    LOADED: 2,
    NETWORK_ERROR: 3,
    SERVER_ERROR: 4
};

var FieldManager = new function() {
    this.state = FieldManagerState.UNINITIALIZED;
    this.onStateChange = null;

    this.cells = [];

    this.setState = function(state) {
        this.state = state;

        if (typeof this.onStateChange !== 'undefined') {
            this.onStateChange(this.state)
        }
    };

    this.requestField = function() {
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            switch (xhr.readyState) {
                case 0: break; // UNINITIALIZED
                case 1: // LOADING
                    FieldManager.setState(FieldManagerState.LOADING);
                    break;
                case 2: break; // LOADED
                case 3: break; // INTERACTIVE
                case 4: // COMPLETED
                    if (xhr.status == 200) {
                        FieldManager.onRequestResult(xhr.responseText);
                    }
                    else
                        FieldManager.setState(FieldManagerState.NETWORK_ERROR);

                    break;
                default: FieldManager.setState(FieldManagerState.NETWORK_ERROR);
            }
        };

        xhr.open("GET", "http://localhost:8080/online-minesweeper/field", true);
        xhr.send(null);
    };

    this.onRequestResult = function(response) {
        try {
            this.cells = JSON.parse(response).cells;

            //if (this.mazeData.status == 0)
                this.setState(FieldManagerState.LOADED);
            //else
            //    this.setState(FieldManagerState.SERVER_ERROR);
        } catch (exception) {
            this.setState(FieldManagerState.SERVER_ERROR);
        }
    }
};