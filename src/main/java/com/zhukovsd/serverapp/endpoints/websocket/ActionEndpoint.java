package com.zhukovsd.serverapp.endpoints.websocket;

import com.zhukovsd.serverapp.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.WebSocketSessionsConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
// Web container creates ServerEndpoint instance for every websocket connection
@ServerEndpoint(value = "/action", configurator = ActionEndpointConfigurator.class)
public class ActionEndpoint {
    // this variables are thread safe due to individual server endpoint instance for each websocket connection
    private Session wsSession;
    private HttpSession httpSession;

    private SessionsCacheConcurrentHashMap sessionsCacheMap;
    //    private UserCacheMap userCacheMap;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.wsSession = session;
        httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

        sessionsCacheMap = (SessionsCacheConcurrentHashMap) httpSession.
                getServletContext().getAttribute("sessions_cache");

        String userId = ((String) httpSession.getAttribute("user_id"));

        WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

        // might be null if session was just destroyed
        if (webSocketSessionsMap != null) {
            webSocketSessionsMap.put(session.getId(), this);

            String s = "";
            for (Map.Entry<String, ActionEndpoint> entry : webSocketSessionsMap.entrySet()) {
                if (s != "") s += ", ";
                s += entry.getValue().wsSession.getId();
            }

            for (Map.Entry<String, ActionEndpoint> entry : webSocketSessionsMap.entrySet()) {
                Session sess = entry.getValue().wsSession;

                try {
                    if (sess.isOpen())
                        sess.getAsyncRemote().sendText(s);
//                        else
                    // TODO: 12.04.2016 remove closed sessions? closed sessions may remain after server restart
//                            System.out.println(3);
                } catch (Exception e) {
                    // sending message on closing session (isOpen is still true) may cause exception
//                        System.out.println(1);
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
//        System.out.println("error");

        close();

//        UserCache userCache = userCacheMap.lockKey(httpSession.getId());
//        try {
//            userCache.webSocketSessions.remove(session);
//        } finally {
//            userCacheMap.unlock();
//        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("close, reason code = " + closeReason.getCloseCode());

        // TODO: 12.04.2016 consider reactions to different reason codes
//        if (closeReason.getCloseCode().getCode() != 1001) {
            // might be null if session was just destroyed

            // TODO: 13.04.2016 check if session is invalidated

            String userId = ((String) httpSession.getAttribute("user_id"));

            WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

            // might be null if session was just destroyed
            if (webSocketSessionsMap != null) {
                webSocketSessionsMap.remove(session.getId(), this);

                String s = "";
                for (Map.Entry<String, ActionEndpoint> entry : webSocketSessionsMap.entrySet()) {
                    if (s != "") s += ", ";
                    s += entry.getValue().wsSession.getId();
                }

                for (Map.Entry<String, ActionEndpoint> entry : webSocketSessionsMap.entrySet()) {
                    Session sess = entry.getValue().wsSession;

                    try {
                        if (sess.isOpen())
                            sess.getAsyncRemote().sendText(s);
    //                        else
                        // TODO: 12.04.2016 remove closed sessions? closed sessions may remain after server restart
    //                            System.out.println(3);
                    } catch (Exception e) {
                        // sending message on closing session (isOpen is still true) may cause exception
    //                        System.out.println(1);
                    }
                }
            }
//        }
    }

    public void close() {
        System.out.println("sup");

        if (wsSession != null) {
            try {
                if (wsSession.isOpen())
                    wsSession.close();
            } catch (Exception e) {
                System.out.println(123);

//                LOGGER.warning(format("Error closing session: %s", e.getMessage()));
            }
        }
    }
}
