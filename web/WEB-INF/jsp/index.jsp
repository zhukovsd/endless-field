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
    <script src="${pageContext.request.contextPath}/js/FieldViewLayerImageData.js"></script>
    <script src="${pageContext.request.contextPath}/js/AbstractFieldViewLayer.js"></script>
    <script src="${pageContext.request.contextPath}/js/CellsFieldViewLayer.js"></script>
    <script src="${pageContext.request.contextPath}/js/PlayersLabelsFieldViewLayer.js"></script>
    <script src="${pageContext.request.contextPath}/js/MouseEventListener.js"></script>

    <script src="${pageContext.request.contextPath}/js/ChunkIdGenerator.js"></script>
    <script src="${pageContext.request.contextPath}/js/Camera.js"></script>
    <script src="${pageContext.request.contextPath}/js/CameraPosition.js"></script>
    <script src="${pageContext.request.contextPath}/js/Scope.js"></script>
    <script src="${pageContext.request.contextPath}/js/ChunksScope.js"></script>
    <script src="${pageContext.request.contextPath}/js/AddressBarManager.js"></script>
    <script src="${pageContext.request.contextPath}/js/ActionMessage.js"></script>
    <script src="${pageContext.request.contextPath}/js/SimpleBiMap.js"></script>

    <script src="${pageContext.request.contextPath}/js/SimpleField/SimpleFieldManager.js"></script>
    <script src="${pageContext.request.contextPath}/js/SimpleField/SimpleMouseEventListener.js"></script>
    <script src="${pageContext.request.contextPath}/js/SimpleField/SimpleCellsFieldViewLayer.js"></script>

    <title>Title</title>
    <script>
        var contextPath = "${pageContext.request.contextPath}";

        var fieldManager = new SimpleFieldManager(contextPath);
        var fieldView = new FieldView(fieldManager, 'field-canvas-container', new DrawSettings(25, 25));
        fieldView.addLayer('cells-layer', new SimpleCellsFieldViewLayer(fieldView, 'field-cells-layer-canvas'));
        fieldView.addLayer('players-labels-layer', new PlayersLabelsFieldViewLayer(fieldView, 'field-players-labels-layer-canvas'));

        var mouseEventListener = new SimpleMouseEventListener(fieldView, 'players-labels-layer');
        var uriManager = new AddressBarManager(contextPath + '/game/');

        fieldView.getLayer('players-labels-layer').mouseListener = mouseEventListener;

        window.addEventListener('load',
            function(event) {
                var canvas = document.getElementById('field-cells-layer-canvas');

                window.addEventListener('resize',
                        function() {
                            canvas.width = canvas.clientWidth;
                            canvas.height = canvas.clientHeight;

                            var scope = fieldView.camera.cellsScope();
                            document.getElementById('canvas-size').textContent = canvas.width + ", " + canvas.height;
                            document.getElementById('camera-position').textContent = JSON.stringify(fieldView.camera.position);
                            document.getElementById('camera-scope').textContent = JSON.stringify(scope);
                            document.getElementById('chunks-scope').textContent = JSON.stringify(scope.chunkIds(fieldManager.chunkSize, fieldManager.chunkIdFactor));

                            fieldView.getLayer('cells-layer').renderByChunkIds([0, 1]);
                        },
                        false
                );
            }, false
        );

        fieldManager.onStateChange = function() {
            switch (fieldManager.state) {
                case (FieldManagerState.CONNECTED): {
                    if (localStorage["cameraPosition"] !== undefined) {
                        var storedPosition = JSON.parse(localStorage["cameraPosition"]);
                    }

                    var uriChunkId = uriManager.getChunkId();

                    var cameraPosition;
                    if (uriChunkId == null) {
                        // if no chunk id specified in the URI (/path/game/<chunkId>)
                        cameraPosition = new CameraPosition(fieldManager.initialChunkId, 0, 0);
                    } else {
                        // if stored chunk id differs from uri chunk id, set camera to left-top corner of uri id
                        if ((storedPosition === undefined) || (uriChunkId != storedPosition.originChunkId)) {
                            cameraPosition = new CameraPosition(uriChunkId, 0, 0);
                        } else {
                            cameraPosition = new CameraPosition(
                                    storedPosition.originChunkId, storedPosition.shift.x, storedPosition.shift.y
                            );
                        }
                    }

                    fieldView.camera.setPosition(cameraPosition);
                    fieldView.updateExpandedScopeChunkIds();
                }

//                case (FieldManagerState.LOADED): {
//                     fieldView.paint();
//                }
            }
        };

        // todo: remember chunks somewhere
        var a;

        var cellsLayer = fieldView.getLayer('cells-layer');
        var labelsLayer = fieldView.getLayer('players-labels-layer');
        fieldManager.onChunksReceived = function(chunkIds) {
            a = chunkIds;

            fieldView.forEachLayer(function(layer) {
                layer.renderByChunkIds(chunkIds);
                layer.display();
            });
//            fieldView.getLayer('cells-layer').renderByChunkIds(chunkIds);
        };

        fieldManager.OnActionMessageReceived = function (positions) {
//            fieldView.forEachLayer(function(layer) {
//                layer.renderByPositions(positions);
//                layer.display();
//            });

            cellsLayer.renderByPositions(positions);
            labelsLayer.renderByChunkIds(a);

            fieldView.forEachLayer(function(layer) {
                layer.display();
            });

//            fieldView.getLayer('cells-layer').drawByPositions(positions);
//            fieldView.getLayer('players-labels-layer').drawVisiblePlayersLabels();
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

    <div class="unselectable" id="field-canvas-container">
        <canvas id="field-cells-layer-canvas"></canvas>
        <canvas id="field-players-labels-layer-canvas"></canvas>
    </div>

    <div style="position: absolute; left: 20px; top: 20px; width: 600px; height: 200px; background-color: rgba(240, 255, 255, 0.8); z-index: 100;">
        <h3>Hi! Your session Id = <%= request.getSession().getId() %></h3>
        <div>canvas size = <span id="canvas-size"></span></div>
        <div>camera position = <span id="camera-position"></span></div>
        <div>camera scope = <span id="camera-scope"></span></div>
        <div>chunks scope = <span id="chunks-scope"></span></div>
        <input type="button" value="requestChunks()" onclick="fieldManager.requestChunks();">
        <input type="button" value="draw" onclick="fieldView.getLayer('cells-layer').display(); /*fieldView.renderByChunkIds([0, 1]);*/">
        <input type="text" name="chunk" id="chunk_id_text" value="0">
    </div>

    <%--<img id="test-img" style="position: absolute; left: 700px; top: 20px; width: 480px; height: 240px; z-index: 100; display: none;"--%>
         <%--src="${pageContext.request.contextPath}/cute-kitty-1920x1080-480x240.jpg"/>--%>

    <%--<input type="button" value="Button" onclick="fieldManager.foo({});">--%>
</body>
</html>
