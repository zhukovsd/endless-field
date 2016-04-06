package com.zhukovsd;

import com.google.gson.Gson;

import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class CellPosition {
    private static Gson gson = new Gson();

    int row = -1;
    int column = -1;

    public CellPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CellPosition that = (CellPosition) o;

        if (row != that.row) return false;
        return column == that.column;

    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }

    static CellPosition createFromJSON(String json) {
        return gson.fromJson(json, CellPosition.class);
    }
}

class ClientID {
    private static Gson gson = new Gson();

    String id;

    public ClientID(String id) {
        this.id = id;
    }

    String toJson() {
        return gson.toJson(this);
    }
}

class ClientData {
    Session session;
    ClientScope scope = new ClientScope();

    public ClientData(Session session) {
        this.session = session;
    }
}

/**
 * Created by ZhukovSD on 27.11.2015.
 */
@ServerEndpoint(value="/action")
public class ActionWebSocketServlet {
    static final Map<String, ClientData> userSessions = Collections.synchronizedMap(new HashMap<>());

    /**
     * Callback hook for Connection open events. This method will be invoked when a
     * client requests for a WebSocket connection.
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) throws InterruptedException {
        userSessions.put(userSession.getId(), new ClientData(userSession));

        System.out.println("new session with id = " + userSession.getId() + ", client count = " + userSessions.size());

//        Thread.sleep(5000);

        userSession.getAsyncRemote().sendText(new ClientID(userSession.getId()).toJson());
    }

    /**
     * Callback hook for Connection close events. This method will be invoked when a
     * client closes a WebSocket connection.
     * @param userSession the userSession which is opened.
     */
    @OnClose
    public void onClose(Session userSession) {
        userSessions.remove(userSession.getId());

        System.out.println("session with id = " + userSession.getId() + " closed, client count = " + userSessions.size());
    }

    @OnError
    public void onError(Session session, Throwable thr) {
//        System.out.println(thr.getMessage());
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client
     * send a message.
     * @param message The text message
     * @param userSession The session of the client
     */
    @OnMessage
    public void onMessage(String message, Session userSession) throws InterruptedException {
        System.out.println("Message Received: " + message);

//        Random rand = new Random();
//        int row = rand.nextInt(10);
//        int column = rand.nextInt(10);

        CellPosition cellPosition = CellPosition.createFromJSON(message);
        FieldServletResponse response = new FieldServletResponse();

//        System.out.println("entering...");
        // synchronize
        synchronized (FieldServlet.field) {
//            System.out.println("entered");
            FieldCell cell = FieldServlet.field.getCell(cellPosition.row, cellPosition.column);
            response.addCell(cellPosition.row, cellPosition.column, cell);
            cell.isChecked = !cell.isChecked;

//            System.out.println("sleeping...");
//            Thread.sleep(5000);
        }
        // synchronize

        // TODO: 07.12.2015 synchronize?
        // synchronize
        for (ClientData clientData : userSessions.values()) {
            ResponseCell cell = response.cells.get(0);
            if (clientData.scope.isCellInScope(cell.row, cell.column)) {
                System.out.println("Sending to " + clientData.session.getId());
                clientData.session.getAsyncRemote().sendText(response.toJson());
            }
        }
        // synchronize
    }
}
