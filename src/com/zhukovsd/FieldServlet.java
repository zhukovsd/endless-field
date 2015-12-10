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
import java.util.HashSet;
import java.util.Set;

class ClientScope {
    // TODO: 07.12.2015 add isEmpty method
    int originRow = -1, originColumn, rowCount, columnCount;

    Set<CellPosition> toSet() {
        Set<CellPosition> result = new HashSet<>();

        for (int i = originRow; i < originRow + rowCount; i++) {
            for (int j = originColumn; j < originColumn + columnCount; j++) {
                result.add(new CellPosition(i, j));
            }
        }

        return result;
    }

    Set<CellPosition> difference(ClientScope scope) {
        Set<CellPosition> result = scope.toSet();
        result.removeAll(this.toSet());

        return result;
    }

    boolean isCellInScope(int row, int column) {
        return (row >= originRow) && (row < originRow + rowCount)
                && (column >= originColumn) && (column < originColumn + columnCount);
    }
}

class FieldServletRequest {
    private static Gson gson = new Gson();

    String clientID;
    ClientScope scope;

    static FieldServletRequest createFromJSON(String json) {
        return gson.fromJson(json, FieldServletRequest.class);
    }
}

@WebServlet("/field")
public class FieldServlet extends HttpServlet {
    static final Field field = new Field();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FieldServletRequest request = FieldServletRequest.createFromJSON(URLDecoder.decode(req.getParameter("data"), "UTF-8"));

        // identify client, save it's scope
        FieldServletResponse response = new FieldServletResponse();

        // synchronize
        synchronized (field) {
            ClientData clientData = ActionWebSocketServlet.userSessions.get(request.clientID);

            Set<CellPosition> responseCells = clientData.scope.difference(request.scope);

            for (CellPosition cellPosition : responseCells) {
                int row = cellPosition.row;
                int column = cellPosition.column;

                response.addCell(row, column, field.getCell(row, column));
            }

            // TODO: 07.12.2015 synchronize?
            clientData.scope = request.scope;

//            for (int i = scope.originRow; i < scope.originRow + scope.rowCount; i++) {
//                for (int j = scope.originColumn; j < scope.originColumn + scope.columnCount; j++) {
//                    response.addCell(i, j, field.getCell(i, j));
//                }
//            }

            // field.getCell(1, 1).isChecked = true;
            System.out.println(responseCells.size() + " cells sent to client #" + request.clientID);
        }
        // synchronize

        resp.getOutputStream().write(response.toJson().getBytes());
    }
}
