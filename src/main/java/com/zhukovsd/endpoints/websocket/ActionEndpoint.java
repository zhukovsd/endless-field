package com.zhukovsd.endpoints.websocket;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
@ServerEndpoint(value = "/action", configurator = ActionEndpointConfigurator.class)
public class ActionEndpoint {
    private Session wsSession;
    private HttpSession httpSession;

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        this.wsSession = session;
        this.httpSession = (HttpSession) config.getUserProperties()
                .get(HttpSession.class.getName());

        System.out.printf("http session id = %s, websocket session id = %s\n", httpSession.getId(), wsSession.getId());
    }

    // TODO: 08.04.2016 disconnect event, error event
}
