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
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldAction;
import com.zhukovsd.endlessfield.field.EndlessFieldActionInvoker;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.serverapp.cache.scopes.UsersByChunkConcurrentCollection;
import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.cache.sessions.WebSocketSessionsConcurrentHashMap;
import com.zhukovsd.serverapp.serialization.EndlessFieldDeserializer;
import com.zhukovsd.serverapp.serialization.EndlessFieldSerializer;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldActionInvoker;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
// Web container creates ServerEndpoint instance for every websocket connection
@ServerEndpoint(value = "/action", configurator = ActionEndpointConfigurator.class)
public class ActionEndpoint {
    // this variables are thread safe due to individual server endpoint instance for each websocket connection

    private Session wsSession;
    private HttpSession httpSession;

    private boolean isSending = false;
    private final LinkedList<String> messagesBuffer = new LinkedList<>();

    private EndlessFieldSerializer serializer;
    private EndlessFieldDeserializer deserializer;
    private SessionsCacheConcurrentHashMap sessionsCacheMap;
    private UsersByChunkConcurrentCollection scopes;
    private EndlessField<? extends EndlessFieldCell> field;

    // This set stores chunks, which was requested with last request to /field HTTP servlet.
    // This set modified by UserScopeConcurrentCollection methods with synchronization on this set.
    // No iteration allowed, because set is not concurrent, and modified in HTTP request thread,
    // not in ws request thread.
    public final Set<Integer> scope = new HashSet<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        wsSession = session;
        httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

        System.out.println("session opened, id = " + session.getId());

        serializer = (EndlessFieldSerializer) httpSession.getServletContext().getAttribute("serializer");
        deserializer = (EndlessFieldDeserializer) httpSession.getServletContext().getAttribute("deserializer");
        sessionsCacheMap = (SessionsCacheConcurrentHashMap) httpSession.getServletContext().getAttribute("sessions_cache");
        scopes = ((UsersByChunkConcurrentCollection) this.httpSession.getServletContext().getAttribute("scopes_cache"));

        // TODO: 06.06.2016 resolve unchecked cast
        field = ((EndlessField<?>) httpSession.getServletContext().getAttribute("field"));
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
//                    wsSession.getAsyncRemote().sendText(serializer.actionEndpointMessageToJSON(message), sendHandler);
                    sendTextMessageAsync(serializer.actionEndpointMessageToJSON(message));
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
        throwable.printStackTrace();

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
//        TimeUnit.SECONDS.sleep(20);

        ClientMessage clientMessage = deserializer.actionMessageDataFromJSON(message);
        ActionServerMessage serverMessage = null;

        EndlessFieldActionInvoker<? extends EndlessFieldCell> invoker = new SimpleFieldActionInvoker(((SimpleField) field));
        EndlessFieldAction action = invoker.selectActionByNumber(clientMessage.type);
        Iterable<Integer> chunkIds = action.getChunkIds(field, clientMessage.cell);

        field.lockChunksByIds(chunkIds);
        try {
            LinkedHashMap<CellPosition, ? extends EndlessFieldCell> entries = action.perform(field, clientMessage.cell);

//            field.updateEntries(entries);

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

        HashSet<ActionEndpoint> recipients = new HashSet<>();

        int c = 0;
        for (Integer chunkId : chunkIds) {
            if (scopes.lockEntry(chunkId)) {
                try {
                    HashSet<ActionEndpoint> endpoints = scopes.getValue(chunkId);
//                    Set<ActionEndpoint> endpoints = Collections.singleton(this);

                    for (ActionEndpoint endpoint : endpoints) {
                        if (!recipients.contains(endpoint)) {
                            sendTextMessageAsync(serialized);

                            recipients.add(endpoint);
                            c++;
                        }
                    }
                } finally {
                    scopes.unlock();
                }
            }
        }

//        System.out.println("message sent to " + c + " endpoints, " + recipients.toString());
    }

    //

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

    private void sendTextMessageAsync(String message) {
        synchronized (messagesBuffer) {
            if (isSending) {
                if (messagesBuffer.size() < 10)
                    messagesBuffer.add(message);
                else {
                    // TODO: 15.06.2016 specify close reason
//                    System.out.println("buffer is full");
                    close();
                }
            } else {
                isSending = true;
                internalSendTextMessageAsync(message);
            }
        }
    }

//    static AtomicInteger messagesSent = new AtomicInteger();

    private void internalSendTextMessageAsync(String message) {
        wsSession.getAsyncRemote().sendText(message, sendHandler);

//        messagesSent.incrementAndGet();
    }

    private final SendHandler sendHandler = sendResult -> {
        if (!sendResult.isOK()) {
            close();
        }

        synchronized (messagesBuffer) {
            if (!messagesBuffer.isEmpty()) {
                internalSendTextMessageAsync(messagesBuffer.remove());
            } else {
                isSending = false;
            }
        }
    };

    @Override
    public String toString() {
        return wsSession.getId();
    }
}