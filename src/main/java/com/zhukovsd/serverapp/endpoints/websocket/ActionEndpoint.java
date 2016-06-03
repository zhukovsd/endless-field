/*
 * Copyright 2016 Zhukov Sergei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhukovsd.serverapp.endpoints.websocket;

import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.cache.sessions.WebSocketSessionsConcurrentHashMap;
import com.zhukovsd.serverapp.serialization.EndlessFieldSerializer;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
// Web container creates ServerEndpoint instance for every websocket connection
@ServerEndpoint(value = "/action", configurator = ActionEndpointConfigurator.class)
public class ActionEndpoint {
    // this variables are thread safe due to individual server endpoint instance for each websocket connection

    private Session wsSession;
    private HttpSession httpSession;

    private EndlessFieldSerializer serializer;
    private SessionsCacheConcurrentHashMap sessionsCacheMap;

    // This set stores chunks, which was requested with last request to /field HTTP servlet.
    // Set modifies in UserScopeConcurrentCollection methods with synchronization on this set.
    // No iteration allowed, because set is not concurrent, and modifies in HTTP request thread,
    // not in ws request thread.
    public final Set<Integer> scope = new HashSet<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        wsSession = session;
        httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

        serializer = (EndlessFieldSerializer) httpSession.getServletContext().getAttribute("serializer");
        sessionsCacheMap = (SessionsCacheConcurrentHashMap) httpSession.getServletContext().getAttribute("sessions_cache");

        String userId = ((String) httpSession.getAttribute("user_id"));

        WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

        // might be null if session was just destroyed
        if (webSocketSessionsMap != null) {
            webSocketSessionsMap.put(session.getId(), this);

            // TODO: 05.05.2016 get real chunk size from field in servlet context
            // TODO: 29.05.2016 determine initial chunk for current user
            ActionEndpointMessage message = new ActionEndpointInitMessage(session.getId(), new ChunkSize(50, 50), 0);

            try {
                if (wsSession.isOpen())
                    wsSession.getAsyncRemote().sendText(serializer.actionEndpointMessageToJSON(message));
            } catch (Exception e) {
                // sending message on closing session (isOpen is still true) may cause exception
//                        System.out.println(1);

                // TODO: 05.05.2016 log smth
            }

//            String s = "";
//            for (Map.Entry<String, ActionEndpoint> entry : webSocketSessionsMap.entrySet()) {
//                if (s != "") s += ", ";
//                s += entry.getValue().wsSession.getId();
//            }
//
//            for (Map.Entry<String, ActionEndpoint> entry : webSocketSessionsMap.entrySet()) {
//                Session sess = entry.getValue().wsSession;
//
//                try {
//                    if (sess.isOpen())
//                        sess.getAsyncRemote().sendText(s);
////                        else
//                    // TODO: 12.04.2016 remove closed sessions? closed sessions may remain after server restart
////                            System.out.println(3);
//                } catch (Exception e) {
//                    // sending message on closing session (isOpen is still true) may cause exception
////                        System.out.println(1);
//                }
//            }
        } else {
            // TODO: 05.05.2016 send no such session error to client
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        close();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("close, reason code = " + closeReason.getCloseCode());

        // TODO: 12.04.2016 consider reactions to different reason codes
//        if (closeReason.getCloseCode().getCode() != 1001) {
            // might be null if session was just destroyed

            // TODO: 13.04.2016 check if session is invalidated

            // TODO: 18.04.2016 check if attribute exists
            String userId = ((String) httpSession.getAttribute("user_id"));

            WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

            // might be null if session was just destroyed
            if (webSocketSessionsMap != null) {
                webSocketSessionsMap.remove(session.getId(), this);

//                String s = "";
//                for (Map.Entry<String, ActionEndpoint> entry : webSocketSessionsMap.entrySet()) {
//                    if (s != "") s += ", ";
//                    s += entry.getValue().wsSession.getId();
//                }
//
//                for (Map.Entry<String, ActionEndpoint> entry : webSocketSessionsMap.entrySet()) {
//                    Session sess = entry.getValue().wsSession;
//
//                    try {
//                        if (sess.isOpen())
//                            sess.getAsyncRemote().sendText(s);
//    //                        else
//                        // TODO: 12.04.2016 remove closed sessions? closed sessions may remain after server restart
//    //                            System.out.println(3);
//                    } catch (Exception e) {
//                        // sending message on closing session (isOpen is still true) may cause exception
//    //                        System.out.println(1);
//                    }
//                }
            }
//        }
    }

    public void close() {
        if (wsSession != null) {
            try {
                if (wsSession.isOpen())
                    wsSession.close();
            } catch (Exception e) {
                // TODO: 05.05.2016 log smth

//                LOGGER.warning(format("Error closing session: %s", e.getMessage()));
            }
        }
    }
}
