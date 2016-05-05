/**
 * Created by ZhukovSD on 04.05.2016.
 */

FieldManagerState = {
    UNINITIALIZED: 0,
    CONNECTED: 1,
    NETWORK_ERROR: 2,
    SERVER_ERROR: 3
};

FieldManager = function() {
    // var - hidden field/function
    // this - accessible field/function

    this.cells = {};

    this.requestField = function(scope) {
        // console.log('requesting cells...');

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

        var requestData = {wsSessionId: 0, scope: [0]};

        xhr.open(
            "GET", "/online-minesweeper/field?data="+
            encodeURIComponent(JSON.stringify(requestData)), true
        );
        xhr.send(null);
    };

    this.onRequestResult = function(response) {
        try {
            var responseCells = JSON.parse(response).cells;

            // alert(response);
            // alert(responseCells);
            // alert(Object.keys(responseCells).length);

            // this.cells = responseCells;

            // alert("length before = " + Object.keys(this.cells).length);            
            
            for (var key in responseCells) {
                this.cells[key] = responseCells[key];
                
                // console.log(key + " = " + responseCells[key]);
            }

            // alert("length after = " + Object.keys(this.cells).length);

            // for (var i = 0; i < responseCells.length; i++) {
            //     var responseCell = responseCells[i];
            //     cells[JSON.stringify({row: responseCell.row, column: responseCell.column})] = responseCell.cell;
            // }

            // alert(JSON.stringify(cells));

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