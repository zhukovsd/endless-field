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

var FieldManager = function () {
    this.state = FieldManagerState.UNINITIALIZED;
    this.onStateChange = null;

    this.cells = {};

    //

    this.wsSessionId = "";
    this.chunkSize = {};    
    
    var webSocket = new WebSocket("ws://" + location.host + "/online-minesweeper/action");
    webSocket.manager = this;

    webSocket.onmessage = function(message) {
        var msg = JSON.parse(message.data);

        if (msg.type === ActionMessageType.INIT_MESSAGE) {
            alert("hi, " + msg.type + ", " + msg.wsSessionId + ", " + msg.chunkSize.rowCount + ", " + msg.chunkSize.columnCount);

            this.manager.wsSessionId = msg.wsSessionId;
            this.manager.chunkSize = msg.chunkSize;
            
            this.manager.setState(FieldManagerState.CONNECTED);
        } else {
            // action message
        }
    };

    // todo: handle on close and on error events

    //

    this.requestField = function(scope) {
        console.log('requesting cells...');

        // todo: request chunks only is state is connected

        // var manager = this;

        var xhr = new XMLHttpRequest();
        xhr.manager = this;
        xhr.onreadystatechange = this.xhronreadystatechange;

        var chunkId = document.getElementById("chunk_id_text").value;
        alert("chunkId = "+ chunkId);

        var requestData = {wsSessionId: this.wsSessionId, scope: [chunkId]};

        xhr.open(
            "GET", "/online-minesweeper/field?data="+
            encodeURIComponent(JSON.stringify(requestData)), true
        );
        xhr.send(null);
    };

    this.xhronreadystatechange = function() {
        switch (this.readyState) {
            case 0: break; // UNINITIALIZED
            case 1: // LOADING
                //manager.setState(FieldManagerState.LOADING);
                break;
            case 2: break; // LOADED
            case 3: break; // INTERACTIVE
            case 4: // COMPLETED
                if (this.status == 200) {
                    this.manager.onRequestResult(this.responseText);
                }
                else
                    this.manager.setState(FieldManagerState.NETWORK_ERROR);

                break;
            default: this.manager.setState(FieldManagerState.NETWORK_ERROR);
        }
    };

    this.onRequestResult = function(response) {
        try {
            //noinspection JSUnresolvedVariable
            var chunks = JSON.parse(response).chunks;
            var manager = this;

            chunks.forEach(function(chunk) {
                manager.processResponseCells(chunk.origin, chunk.cells);
            });

            alert("cells count = " + Object.keys(this.cells).length);
            // alert("0, 0 = " + JSON.stringify(this.cells["0,0"]));
            // alert("0, 1 = " + JSON.stringify(this.cells["0,1"]));

            //if (this.mazeData.status == 0)
            //    this.setState(FieldManagerState.LOADED);
            // fieldView.paint();
            //else
            //    this.setState(FieldManagerState.SERVER_ERROR);
        } catch (exception) {
            this.setState(FieldManagerState.SERVER_ERROR);
        }
    }
};

FieldManager.prototype = {
    foo: function() {
        alert(this.wsSessionId);
    },

    processResponseCells: function(chunkOrigin, responseCells) {
        //alert(this.chunkSize.rowCount + ", " + this.chunkSize.columnCount);
        //alert("chunkOrigin = " + JSON.stringify(chunkOrigin));
        
        var index = 0;
        for (var row = 0; row < this.chunkSize.rowCount; row++) {
            for (var column = 0; column < this.chunkSize.columnCount; column++) {
                var key = (chunkOrigin.row + row) + "," + (chunkOrigin.column + column);
                //console.log(key);
                this.cells[key] = this.processResponseCell(responseCells[index]);

                index++;
            }
        }
    },

    processResponseCell: function(cell) {
        return cell;
    }
};