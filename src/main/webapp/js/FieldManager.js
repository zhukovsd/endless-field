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
 * Created by ZhukovSD on 04.05.2016.
 */

var FieldManagerState = {
    UNINITIALIZED: 0,
    CONNECTED: 1,
    NETWORK_ERROR: 2,
    SERVER_ERROR: 3
};

var ActionMessageType = {
    INIT_MESSAGE: 0,
    ACTION_MESSAGE: 1
};

var Player = function(id, name) {
    this.id = id;
    this.name = name;

    this.toString = function() {
        return this.id;
    }
};

var FieldManager = function (applicationContextPath) {
    this.state = FieldManagerState.UNINITIALIZED;
    
    this.onStateChange = null;
    this.onChunksReceived = null;
    this.OnActionMessageReceived = null;

    this.cells = {};
    this.playersPositions = new SimpleBiMap(); // BiMap<Position, Player>

    // var player = new Player('0', 'User #0');
    // this.playersPositions[player.toString()] = {player: player, position: new CellPosition(20, 10)};

    this.setState = function(state) {
        this.state = state;

        if (this.onStateChange != null) {
            this.onStateChange(this.state);
        }
    };
    
    //

    this.wsSessionId = null;
    this.userId = null;
    this.chunkSize = {};
    this.chunkIdFactor = 0;
    this.initialChunkId = 0;
    
    var webSocket = new WebSocket("ws://" + location.host + applicationContextPath + "/action");
    webSocket.manager = this;

    webSocket.onmessage = function(message) {
        var msg = JSON.parse(message.data);

        if (msg.type === ActionMessageType.INIT_MESSAGE) {
            // alert("hi, " + msg.type + ", " + msg.wsSessionId + ", " + msg.chunkSize.rowCount + ", " + msg.chunkSize.columnCount);
            // alert(JSON.stringify(msg));
            console.log(message.data);

            this.manager.wsSessionId = msg.wsSessionId;
            this.manager.userId = msg.userId;
            this.manager.chunkSize = msg.chunkSize;
            this.manager.chunkIdFactor = msg.chunkIdFactor;
            this.manager.initialChunkId = msg.initialChunkId;
            
            this.manager.setState(FieldManagerState.CONNECTED);
        } else if (msg.type === ActionMessageType.ACTION_MESSAGE) {
            // action message
            var cells = msg.cells;
            var positions = {};

            var c = 0;
            for (var key in cells) {
                if (cells.hasOwnProperty(key)) {
                    c++;

                    var position = new CellPosition().fromKey(key);
                    this.manager.cells[position.toString()] = this.manager.processResponseCell(cells[key]);
                    // todo remove debug field
                    this.manager.cells[position.toString()].text = key + "!";

                    positions[position.toString()] = position;
                }
            }
            console.log(c + "cells updated");

            var player = new Player(msg.player.id, msg.player.name);
            if (player.id !== this.manager.userId) {                
                this.manager.playersPositions.put(new CellPosition(msg.origin.row, msg.origin.column), player);
            }
            
            if (this.manager.OnActionMessageReceived !== null) {
                this.manager.OnActionMessageReceived(positions);
            }
        }
    };

    // todo: handle on close and on error events

    this.sendMessage = function(message) {
        if (this.state = FieldManagerState.CONNECTED) {
            webSocket.send(JSON.stringify(message));
        }
    };

    //

    this.requestChunks = function(chunkIds) {
        console.log('requesting chunks ' + chunkIds);

        // todo: request chunks only is state is connected
        var xhr = new XMLHttpRequest();
        xhr.manager = this;
        xhr.onreadystatechange = this.xhronreadystatechange;

        //var chunkId = document.getElementById("chunk_id_text").value;
        //alert("chunkId = "+ chunkId);

        var requestData = {wsSessionId: this.wsSessionId, scope: chunkIds};

        xhr.open(
            "GET", applicationContextPath + "/field?data="+
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

            var chunkIds = [];

            chunks.forEach(function(chunk) {
                chunkIds.push(ChunkIdGenerator.generateId(manager.chunkSize, manager.chunkIdFactor, chunk.origin));

                manager.processResponseCells(chunk.origin, chunk.cells);
            });

            console.log("chunk received, ids = " + chunkIds + ", current cells count = " + Object.keys(this.cells).length);
            // alert("cells count = " + Object.keys(this.cells).length);

            //todo draw only new cells
            if (this.onChunksReceived !== null) {
                this.onChunksReceived(chunkIds);
            }

            // alert("0, 0 = " + JSON.stringify(this.cells["0,0"]));
            // alert("0, 1 = " + JSON.stringify(this.cells["0,1"]));

            //if (this.mazeData.status == 0)
            //    this.setState(FieldManagerState.LOADED);
            // fieldView.paint();
            //else
            //    this.setState(FieldManagerState.SERVER_ERROR);
        } catch (exception) {
            console.log('exception ' + exception.stack);
            this.setState(FieldManagerState.SERVER_ERROR);

            throw exception;
        }
    };
    
    this.getCell = function(row, column) {
        return this.cells[row + "," + column];
    };
};

FieldManager.prototype = {
    processResponseCells: function(chunkOrigin, responseCells) {
        //alert(this.chunkSize.rowCount + ", " + this.chunkSize.columnCount);
        //alert("chunkOrigin = " + JSON.stringify(chunkOrigin));
        
        var index = 0;
        for (var row = 0; row < this.chunkSize.rowCount; row++) {
            for (var column = 0; column < this.chunkSize.columnCount; column++) {
                var key = (chunkOrigin.row + row) + "," + (chunkOrigin.column + column);
                // console.log(key);
                this.cells[key] = this.processResponseCell(responseCells[index]);
                
                // todo remove debug field
                this.cells[key].text = key;

                index++;
            }
        }
    },

    processResponseCell: function(cell) {
        return cell;
    }
};