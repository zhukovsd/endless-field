package com.zhukovsd;

import com.google.gson.Gson;
import com.sun.deploy.util.SessionState;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

class ClientScope {
    private static Gson gson = new Gson();

    int originRow, originColumn, rowCount, columnCount;

    static ClientScope createFromJSON(String json) {
        return gson.fromJson(json, ClientScope.class);
    }
}

@WebServlet("/field")
public class FieldServlet extends HttpServlet {
    static Field field = new Field();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ClientScope scope =  ClientScope.createFromJSON(URLDecoder.decode(req.getParameter("scope"), "UTF-8"));

        FieldServletResponse response = new FieldServletResponse();
        for (int i = scope.originRow; i < scope.originRow + scope.rowCount; i++) {
            for (int j = scope.originColumn; j < scope.originColumn + scope.columnCount; j++) {
                response.addCell(i, j, field.getCell(i, j));
            }
        }

        field.getCell(1, 1).isChecked = true;


        resp.getOutputStream().write(response.toJson().getBytes());
    }
}
