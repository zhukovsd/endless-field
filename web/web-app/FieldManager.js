/**
 * Created by ZhukovSD on 25.11.2015.
 */

FieldManagerState = {
    UNINITIALIZED: 0,
    CONNECTED: 1,
    NETWORK_ERROR: 2,
    SERVER_ERROR: 3
};

FieldManager = function() {
    this.id = -1;

    this.state = FieldManagerState.UNINITIALIZED;
    this.onStateChange = null;

    var webSocket = new WebSocket("ws://" + location.host + "/online-minesweeper/action");
    webSocket.manager = this;

    webSocket.onmessage = function(message) {
        var msg = JSON.parse(message.data);

        // ?
        if (typeof msg.id === 'undefined')
            this.manager.onRequestResult(message.data);
        else {
            // alert(msg.id);
            this.manager.id = msg.id;
            this.manager.setState(FieldManagerState.CONNECTED);
        }
    };

    webSocket.onerror = function() { };

    //webSocket.onopen = function(){
    //};

    var cells = [];

    this.setState = function(state) {
        this.state = state;

        if (this.state = FieldManagerState.CONNECTED)
            fieldManager.requestField(fieldView.cellsScope());

        if (typeof this.onStateChange !== 'undefined') {
            this.onStateChange(this.state);
        }
    };

    this.requestField = function(scope) {
        console.log('requesting cells...');

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

        var requestData = {clientID: this.id, scope: scope};

        xhr.open(
            "GET", "/online-minesweeper/field?data="+
            encodeURIComponent(JSON.stringify(requestData)), true
        );
        xhr.send(null);
    };

    this.onRequestResult = function(response) {
        try {
            var responseCells = JSON.parse(response).cells;

            for (var i = 0; i < responseCells.length; i++) {
                var responseCell = responseCells[i];
                cells[JSON.stringify({row: responseCell.row, column: responseCell.column})] = responseCell.cell;
            }

            //this.getCell(1, 1);
            //this.getCell(1, 2);

            //if (this.mazeData.status == 0)
            //    this.setState(FieldManagerState.LOADED);
                fieldView.paint();
            //else
            //    this.setState(FieldManagerState.SERVER_ERROR);
        } catch (exception) {
            this.setState(FieldManagerState.SERVER_ERROR);
        }
    };

    this.getCell = function(row, column) {
        return cells[JSON.stringify({row: row, column: column})];
    };

    this.cellClick = function(cellPosition) {
        if (cellPosition != null) {
            webSocket.send(JSON.stringify(cellPosition));

            //console.log("hi");
        }
    };

    this.test = function() {
        webSocket.send("test");
    }
};