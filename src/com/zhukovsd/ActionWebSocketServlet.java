package com.zhukovsd;

import java.io.IOException;
import java.util.*;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.*;

/**
 * Created by ZhukovSD on 27.11.2015.
 */
@ServerEndpoint(value="/action")
public class ActionWebSocketServlet {
    private static Set<Session> userSessions = Collections.synchronizedSet(new HashSet<>());

    /**
     * Callback hook for Connection open events. This method will be invoked when a
     * client requests for a WebSocket connection.
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        userSessions.add(userSession);

        System.out.println("new session with id = " + userSession.getId() + ", client count = " + userSessions.size());
    }

    /**
     * Callback hook for Connection close events. This method will be invoked when a
     * client closes a WebSocket connection.
     * @param userSession the userSession which is opened.
     */
    @OnClose
    public void onClose(Session userSession) {
        userSessions.remove(userSession);

        System.out.println("session with id = " + userSession.getId() + " closed, client count = " + userSessions.size());
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client
     * send a message.
     * @param message The text message
     * @param userSession The session of the client
     */
    @OnMessage
    public void onMessage(String message, Session userSession) {
        System.out.println("Message Received: " + message);

        Random rand = new Random();
        int row = rand.nextInt(10);
        int column = rand.nextInt(10);

        // synchronize
        FieldServletResponse response = new FieldServletResponse();
        response.addCell(row, column, FieldServlet.field.getCell(row, column));
        FieldServlet.field.getCell(row, column).isChecked = true;
        // synchronize

        for (Session session : userSessions) {
            System.out.println("Sending to " + session.getId());
            session.getAsyncRemote().sendText(response.toJson());
        }
    }
}
