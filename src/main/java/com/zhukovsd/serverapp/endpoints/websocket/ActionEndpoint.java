package com.zhukovsd.serverapp.endpoints.websocket;

import com.zhukovsd.serverapp.UserCache;
import com.zhukovsd.serverapp.UserCacheMap;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
// Web container creates ServerEndpoint instance for every websocket connection
@ServerEndpoint(value = "/action", configurator = ActionEndpointConfigurator.class)
public class ActionEndpoint {
    // this variables are thread safe due to individual server endpoint instance for each websocket connection
//    private Session wsSession;
    private UserCacheMap userCacheMap;
//    private HttpSession httpSession;

    @OnOpen
    public void open(Session session, EndpointConfig config) {
//        this.wsSession = session;

        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
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
                    wsSession.getAsyncRemote().sendText(s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } finally {
            userCacheMap.unlock();
        }
//        System.out.printf("http session id = %s, websocket session id = %s\n", httpSession.getId(), wsSession.getId());
    }

    // TODO: 08.04.2016 disconnect event, error event
}
