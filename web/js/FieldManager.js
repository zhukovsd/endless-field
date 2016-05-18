/**
 * Created by ZhukovSD on 04.05.2016.
 */

FieldManagerState = {
    UNINITIALIZED: 0,
    CONNECTED: 1,
    NETWORK_ERROR: 2,
    SERVER_ERROR: 3
};

ActionMessageType = {
    INIT_MESSAGE: 0,
    ACTION_MESSAGE: 1
};

FieldManager = function() {
    console.log("1");

    // var - hidden field/function
    // this - accessible field/function

    this.state = FieldManagerState.UNINITIALIZED;
    this.onStateChange = null;

    this.cells = {};

    //

    var wsSessionId = "";
    var webSocket = new WebSocket("ws://" + location.host + "/online-minesweeper/action");
    webSocket.manager = this;

    webSocket.onmessage = function(message) {
        var msg = JSON.parse(message.data);

        if (msg.type === ActionMessageType.INIT_MESSAGE) {
            // alert("hi, " + msg.type + ", " + msg.wsSessionId);

            wsSessionId = msg.wsSessionId;
        } else {
            // alert(msg.id);
            this.manager.id = msg.id;
            this.manager.setState(FieldManagerState.CONNECTED);

            // this.manager.onRequestResult(message.data);
        }
    };

    // todo: handle on close and on error events

    //

    this.requestField = function(scope) {
        // console.log('requesting cells...');

        // todo: request chunks only is state is connected

        var manager = this;

        var xhr = new XMLHttpRequest();

        xhr.onreadystatechange = function() {
            switch (xhr.readyState) {
                case 0: break; // UNINITIALIZED
                case 1: // LOADING
                    //manager.setState(FieldManagerState.LOADING);
                    break;
                case 2: break; // LOADED
                case 3: break; // INTERACTIVE
                case 4: // COMPLETED
                    if (xhr.status == 200) {
                        manager.onRequestResult(xhr.responseText);
                    }
                    else
                        manager.setState(FieldManagerState.NETWORK_ERROR);

                    break;
                default: manager.setState(FieldManagerState.NETWORK_ERROR);
            }
        };

        var requestData = {wsSessionId: wsSessionId, scope: [0]};

        xhr.open(
            "GET", "/online-minesweeper/field?data="+
            encodeURIComponent(JSON.stringify(requestData)), true
        );
        xhr.send(null);
    };

    this.onRequestResult = function(response) {
        try {
            alert("inherited");
            
            var responseCells = JSON.parse(response).cells;
            
            for (var key in responseCells) {
                this.cells[key] = responseCells[key];
            }

            // alert(Object.keys(this.cells).length);

            //this.getCell(1, 1);
            //this.getCell(1, 2);

            //if (this.mazeData.status == 0)
            //    this.setState(FieldManagerState.LOADED);
            // fieldView.paint();
            //else
            //    this.setState(FieldManagerState.SERVER_ERROR);
        } catch (exception) {
            this.setState(FieldManagerState.SERVER_ERROR);
        }
    };
};