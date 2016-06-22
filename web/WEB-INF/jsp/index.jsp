<%--
  ~ Copyright 2016 Zhukov Sergei
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <link href="${pageContext.request.contextPath}/style.css" rel="stylesheet" type="text/css" />

    <script src="${pageContext.request.contextPath}/js/FieldManager.js"></script>
    <script src="${pageContext.request.contextPath}/js/FieldView.js"></script>
    <script src="${pageContext.request.contextPath}/js/MouseEventListener.js"></script>

    <script src="${pageContext.request.contextPath}/js/ChunkIdGenerator.js"></script>
    <script src="${pageContext.request.contextPath}/js/Camera.js"></script>
    <script src="${pageContext.request.contextPath}/js/CameraPosition.js"></script>
    <script src="${pageContext.request.contextPath}/js/Scope.js"></script>
    <script src="${pageContext.request.contextPath}/js/AddressBarManager.js"></script>
    <script src="${pageContext.request.contextPath}/js/ActionMessage.js"></script>

    <title>Title</title>
    <script>
        var contextPath = "${pageContext.request.contextPath}";

        var fieldManager = new FieldManager(contextPath);
        var fieldView = new FieldView(fieldManager, new DrawSettings(25, 25));
        var eventListener = new MouseEventListener(fieldView);
        var uriManager = new AddressBarManager(contextPath + '/game/');

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

//                        scope.removePartiallyVisibleCells();
//                        console.log(JSON.stringify(scope));

                        fieldView.drawCellsByChunkIds([0, 1]);
                    },
                    false
            );
        };

        fieldManager.onStateChange = function() {
            switch (fieldManager.state) {
                case (FieldManagerState.CONNECTED): {
                    var storedPosition = JSON.parse(localStorage["cameraPosition"]);
                    var uriChunkId = uriManager.getChunkId();

                    var cameraPosition;
                    if (uriChunkId == null) {
                        // if no chunk id specified in the URI (/path/game/<chunkId>)
                        cameraPosition = new CameraPosition(fieldManager.initialChunkId, 0, 0);
                    } else {
                        // if stored chunk id differs from uri chunk id, set camera to left-top corner of uri id
                        if (uriChunkId != storedPosition.originChunkId) {
                            cameraPosition = new CameraPosition(uriChunkId, 0, 0);
                        } else {
                            cameraPosition = new CameraPosition(
                                    storedPosition.originChunkId, storedPosition.shift.x, storedPosition.shift.y
                            );
                        }
                    }

                    fieldView.camera.setPosition(cameraPosition);
                    // todo expand scope
                    fieldManager.requestChunks(fieldView.camera.cellsScope().chunkIds(fieldManager.chunkSize, fieldManager.chunkIdFactor));
                }

//                case (FieldManagerState.LOADED): {
//                     fieldView.paint();
//                }
            }
        };

        fieldView.camera.onPositionChanged = function(position) {
            uriManager.setChunkId(position.originChunkId);
            localStorage["cameraPosition"] = JSON.stringify(position);
        };

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
