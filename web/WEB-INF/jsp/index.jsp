<%--
  Created by IntelliJ IDEA.
  User: ZhukovSD
  Date: 07.04.2016
  Time: 20:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <link href="style.css" rel="stylesheet" type="text/css" />

    <script src="js/ChunkIdGenerator.js"></script>
    <script src="js/Scope.js"></script>

    <script src="js/FieldManager.js"></script>
    <script src="js/FieldView.js"></script>

    <script src="js/SimpleField/SimpleFieldManager.js"></script>
    <title>Title</title>
    <script>
//        var fieldManager = new FieldManager();
        var fieldManager = new SimpleFieldManager();
        var fieldView = new FieldView(fieldManager, new DrawSettings(25, 25));

        window.onload = function() {
            fieldView.init('field-canvas-container', 'field-canvas');

            var canvas = document.getElementById('field-canvas');

            window.addEventListener('resize',
                    function() {
                        canvas.width = canvas.clientWidth;
                        canvas.height = canvas.clientHeight;

                        var scope = fieldView.cameraScope();
                        document.getElementById('canvas-size').textContent = canvas.width + ", " + canvas.height;
                        document.getElementById('camera-scope').textContent = JSON.stringify(scope);
                        document.getElementById('chunks-scope').textContent = JSON.stringify(scope.chunkIds(fieldManager.chunkSize, fieldManager.chunkIdFactor));

                        fieldView.drawCellsByChunkIds([0]);
                    },
                    false
            );
        };

//        fieldManager.onRequestResult(null);

//        var webSocket = new WebSocket("ws://" + location.host + "/online-minesweeper/action");
//
//        webSocket.onmessage = function(message) {
//            document.getElementById("web_socket_ids").innerHTML = message.data;
//        };

//        webSocket.onopen = function () {
//            console.log("id = " + webSocket.id);
//        };

        // TODO websocket close/error events
    </script>
</head>
<body>
    <%--Your websocket session ids = [<span id="web_socket_ids"></span>]<br/>--%>
    <%--Scope for this client = 123--%>

    <div id="field-canvas-container">
        <canvas id="field-canvas"></canvas>
    </div>

    <div style="position: absolute; left: 20px; top: 20px; width: 600px; height: 200px; background-color: rgba(240, 255, 255, 0.8);">
        <h3>Hi! Your session Id = <%= request.getSession().getId() %></h3>
        <div>canvas size = <span id="canvas-size"></span></div>
        <div>camera scope = <span id="camera-scope"></span></div>
        <div>chunks scope = <span id="chunks-scope"></span></div>
        <input type="button" value="requestChunks()" onclick="fieldManager.requestChunks();">
        <%--<input type="button" value="scope" onclick="alert(JSON.stringify(fieldView.cameraScope()));">--%>
        <input type="button" value="draw" onclick="fieldView.drawCellsByChunkIds([0]);">
        <input type="text" name="chunk" id="chunk_id_text" value="0">
    </div>

    <%--<input type="button" value="Button" onclick="fieldManager.foo({});">--%>
</body>
</html>
