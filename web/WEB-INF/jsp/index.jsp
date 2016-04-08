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
    </script>
</head>
<body>
<h1>Hi! Your session Id = <%= request.getSession().getId() %></h1>
</body>
</html></html>
