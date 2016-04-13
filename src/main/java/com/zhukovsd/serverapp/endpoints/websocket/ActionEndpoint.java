package com.zhukovsd.serverapp.endpoints.websocket;

import com.zhukovsd.serverapp.UserCache;
import com.zhukovsd.serverapp.UserCacheMap;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
// Web container creates ServerEndpoint instance for every websocket connection
@ServerEndpoint(value = "/action", configurator = ActionEndpointConfigurator.class)
public class ActionEndpoint {
    // this variables are thread safe due to individual server endpoint instance for each websocket connection
//    private Session wsSession;
    private HttpSession httpSession;
    private UserCacheMap userCacheMap;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
//        this.wsSession = session;

//        ConcurrentHashMap.newKeySet<Integer>;
//        ConcurrentHashMap.newKeySet();

        httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        userCacheMap = ((UserCacheMap) httpSession.getServletContext().getAttribute("usercache"));

        UserCache userCache = userCacheMap.lockKey(httpSession.getId());
        try {
            userCache.webSocketSessions.add(session);

            String s = "";
            for (Session wsSession : userCache.webSocketSessions) {
                if (s != "") s += ", ";
                s += wsSession.getId();
            }

            try {
                for (Session wsSession : userCache.webSocketSessions)
                    try {
                        if (wsSession.isOpen())
                            wsSession.getAsyncRemote().sendText(s);
//                        else
                            // TODO: 12.04.2016 remove closed sessions? closed sessions may remain after server restart
//                            System.out.println(3);
                    } catch (Exception e) {
                        // sending message on closing session (isOpen is still true) may cause exception
//                        System.out.println(1);
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            userCacheMap.unlock();
        }
//        System.out.printf("http session id = %s, websocket session id = %s\n", httpSession.getId(), wsSession.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        UserCache userCache = userCacheMap.lockKey(httpSession.getId());
        try {
            userCache.webSocketSessions.remove(session);
        } finally {
            userCacheMap.unlock();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        // TODO: 12.04.2016 consider reactions to different reason codes
        if (closeReason.getCloseCode().getCode() != 1001) {
            UserCache userCache = userCacheMap.lockKey(httpSession.getId());
            try {
                userCache.webSocketSessions.remove(session);

                String s = "";
                for (Session wsSession : userCache.webSocketSessions) {
                    if (s != "") s += ", ";
                    s += wsSession.getId();
                }

                try {
                    for (Session wsSession : userCache.webSocketSessions) {
                        try {
                            if (wsSession.isOpen())
                                wsSession.getAsyncRemote().sendText(s);
//                            else
//                                System.out.println(4);
                        } catch (Exception e) {
//                            System.out.println(2);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                userCacheMap.unlock();
            }
        }
    }

    // TODO: 08.04.2016 disconnect event, error event
}
