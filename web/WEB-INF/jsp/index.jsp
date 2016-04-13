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
    <title>Title</title>
    <script>
        var webSocket = new WebSocket("ws://" + location.host + "/online-minesweeper/action");

        webSocket.onmessage = function(message) {
            document.getElementById("web_socket_ids").innerHTML = message.data;
        };

        // TODO websocket close/error events
    </script>
</head>
<body>
<h1>Hi! Your session Id = <%= request.getSession().getId() %></h1>
Your websocket session ids = [<span id="web_socket_ids"></span>]<br/>
Scope for this client = 123
</body>
</html>
