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

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkIdGenerator;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.serverapp.cache.scopes.UsersByChunkConcurrentCollection;
import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.cache.sessions.WebSocketSessionsConcurrentHashMap;
import com.zhukovsd.serverapp.serialization.EndlessFieldDeserializer;
import com.zhukovsd.serverapp.serialization.EndlessFieldGsonSerializer;
import com.zhukovsd.serverapp.serialization.EndlessFieldSerializer;
import com.zhukovsd.simplefield.SimpleFieldCell;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
// Web container creates ServerEndpoint instance for every websocket connection
@ServerEndpoint(value = "/action", configurator = ActionEndpointConfigurator.class)
public class ActionEndpoint<T extends EndlessFieldCell> {
    // this variables are thread safe due to individual server endpoint instance for each websocket connection

    private Session wsSession;
    private HttpSession httpSession;

    private EndlessFieldSerializer serializer;
    private EndlessFieldDeserializer deserializer;
    private SessionsCacheConcurrentHashMap sessionsCacheMap;
    private UsersByChunkConcurrentCollection scopes;
    private EndlessField<T> field;

    // This set stores chunks, which was requested with last request to /field HTTP servlet.
    // This set modified by UserScopeConcurrentCollection methods with synchronization on this set.
    // No iteration allowed, because set is not concurrent, and modified in HTTP request thread,
    // not in ws request thread.
    public final Set<Integer> scope = new HashSet<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        wsSession = session;
        httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

        serializer = (EndlessFieldSerializer) httpSession.getServletContext().getAttribute("serializer");
        deserializer = (EndlessFieldDeserializer) httpSession.getServletContext().getAttribute("deserializer");
        sessionsCacheMap = (SessionsCacheConcurrentHashMap) httpSession.getServletContext().getAttribute("sessions_cache");
        scopes = ((UsersByChunkConcurrentCollection) this.httpSession.getServletContext().getAttribute("scopes_cache"));

        // TODO: 06.06.2016 resolve unchecked cast
        field = ((EndlessField<T>) httpSession.getServletContext().getAttribute("field"));

        String userId = ((String) httpSession.getAttribute("user_id"));

        WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

        // might be null if session was just destroyed
        if (webSocketSessionsMap != null) {
            webSocketSessionsMap.put(session.getId(), this);

            // TODO: 05.05.2016 get real chunk size from field in servlet context
            // TODO: 29.05.2016 determine initial chunk for current user
            ServerMessage message = new InitServerMessage(session.getId(), new ChunkSize(50, 50), 0);

            try {
                if (wsSession.isOpen())
                    wsSession.getAsyncRemote().sendText(serializer.actionEndpointMessageToJSON(message));
            } catch (Exception e) {
                // sending message on closing session (isOpen is still true) may cause exception
//                        System.out.println(1);

                // TODO: 05.05.2016 log smth
//                e.printStackTrace();
            }
        } else {
            // TODO: 05.05.2016 send no such session error to client
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        close();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws InterruptedException {
        System.out.println("close, reason code = " + closeReason.getCloseCode());

        // TODO: 12.04.2016 consider reactions to different reason codes
//        if (closeReason.getCloseCode().getCode() != 1001) {

        // TODO: 13.04.2016 check if session is invalidated

        // TODO: 18.04.2016 check if attribute exists
        String userId = ((String) httpSession.getAttribute("user_id"));

        WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

        // might be null if session was just destroyed
        if (webSocketSessionsMap != null) {
            webSocketSessionsMap.remove(session.getId(), this);
        }

        // TODO: update <chunk id, endpoints> collection
        scopes.updateEndpointScope(this, Collections.emptySet());
    }

    @OnMessage
    public void onMessage(String message, Session userSession) throws InterruptedException {
//        System.out.println("Message Received: " + message);

        ClientMessage clientMessage = deserializer.actionMessageDataFromJSON(message);
        ActionServerMessage serverMessage = null;

        int chunkId = ChunkIdGenerator.generateID(field.chunkSize, clientMessage.cell);

        field.lockChunksByIds(Collections.singletonList(chunkId));
        try {
            LinkedHashMap<CellPosition, T> entries = field.getEntries(Collections.singletonList(clientMessage.cell));
            for (EndlessFieldCell cell : entries.values()) {
                SimpleFieldCell casted = ((SimpleFieldCell) cell);
                casted.setChecked(!casted.isChecked());
            }

            field.updateEntries(entries);

            HashMap<CellPosition, EndlessFieldCell> cloned = new LinkedHashMap<>(entries.size());
            for (Map.Entry<CellPosition, ? extends EndlessFieldCell> entry : entries.entrySet()) {
                EndlessFieldCell cell = entry.getValue();
                cloned.put(entry.getKey(), cell.getFactory().clone(cell));
            }

            serverMessage = new ActionServerMessage(cloned);
        } finally {
            field.unlockChunks();
        }

        String serialized = serializer.actionEndpointMessageToJSON(serverMessage);

        if (scopes.lockEntry(chunkId)) {
            try {
                HashSet<ActionEndpoint<?>> endpoints = scopes.getValue(chunkId);

                int c = 0;
                for (ActionEndpoint<?> endpoint : endpoints) {
                    endpoint.wsSession.getAsyncRemote().sendText(serialized);
                    c++;
                }

                System.out.println("message sent to " + c + " endpoints");
            } finally {
                scopes.unlock();
            }
        };
    }

    private void close() {
        if (wsSession != null) {
            try {
                if (wsSession.isOpen())
                    wsSession.close();
            } catch (Exception e) {
                // TODO: 05.05.2016 log smth

//                e.printStackTrace();

//                LOGGER.warning(format("Error closing session: %s", e.getMessage()));
            }
        }
    }
}
