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
    <script src="js/Camera.js"></script>
    <script src="js/CameraPosition.js"></script>
    <script src="js/Scope.js"></script>

    <script src="js/FieldManager.js"></script>
    <script src="js/FieldView.js"></script>
    <script src="js/EventListener.js"></script>

    <script src="js/SimpleField/SimpleFieldManager.js"></script>
    <title>Title</title>
    <script>
//        var fieldManager = new FieldManager();
        var fieldManager = new SimpleFieldManager();
        var fieldView = new FieldView(fieldManager, new DrawSettings(25, 25));
        var eventListener = new EventListener(fieldView);

        window.onload = function() {
            fieldView.init('field-canvas-container', 'field-canvas');
            eventListener.init('field-canvas');

            var canvas = document.getElementById('field-canvas');

            window.addEventListener('resize',
                    function() {
                        canvas.width = canvas.clientWidth;
                        canvas.height = canvas.clientHeight;

                        var scope = fieldView.camera.cellsScope();
                        document.getElementById('canvas-size').textContent = canvas.width + ", " + canvas.height;
                        document.getElementById('camera-position').textContent = JSON.stringify(fieldView.camera.position);
                        document.getElementById('camera-scope').textContent = JSON.stringify(scope);
                        document.getElementById('chunks-scope').textContent = JSON.stringify(scope.chunkIds(fieldManager.chunkSize, fieldManager.chunkIdFactor));

                        fieldView.drawCellsByChunkIds([0, 1]);
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
        <div>camera position = <span id="camera-position"></span></div>
        <div>camera scope = <span id="camera-scope"></span></div>
        <div>chunks scope = <span id="chunks-scope"></span></div>
        <input type="button" value="requestChunks()" onclick="fieldManager.requestChunks();">
        <input type="button" value="draw" onclick="fieldView.drawCellsByChunkIds([0, 1]);">
        <input type="text" name="chunk" id="chunk_id_text" value="0">

        <input type="button" value="left" onclick="
            var context = document.getElementById('field-canvas').getContext('2d');

            // shift everything to the left:
            var imageData = context.getImageData(1, 0, context.canvas.width-1, context.canvas.height);
            context.putImageData(imageData, 0, 0);
            // now clear the right-most pixels:
            context.clearRect(context.canvas.width-1, 0, 1, context.canvas.height);
        ">

        <input type="button" value="right" onclick="
            var context = document.getElementById('field-canvas').getContext('2d');

            // shift everything to the right:
            var imageData = context.getImageData(0, 0, context.canvas.width-1, context.canvas.height);
            context.putImageData(imageData, 1, 0);
            // now clear the right-most pixels:
//            context.clearRect(context.canvas.width-1, 0, 1, context.canvas.height);
        ">
    </div>

    <%--<input type="button" value="Button" onclick="fieldManager.foo({});">--%>
</body>
</html>
