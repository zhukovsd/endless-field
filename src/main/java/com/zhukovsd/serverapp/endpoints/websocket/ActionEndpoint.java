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
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldAction;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldCellView;
import com.zhukovsd.serverapp.cache.scopes.UsersByChunkConcurrentCollection;
import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.cache.sessions.WebSocketSessionsConcurrentHashMap;
import com.zhukovsd.serverapp.serialization.EndlessFieldDeserializer;
import com.zhukovsd.serverapp.serialization.EndlessFieldSerializer;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

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

        field = ((EndlessField<?>) httpSession.getServletContext().getAttribute("field"));
        String userId = ((String) httpSession.getAttribute("user_id"));

        WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

        // might be null if session was just destroyed
        if (webSocketSessionsMap != null) {
            webSocketSessionsMap.put(session.getId(), this);

            // TODO: 29.05.2016 determine initial chunk for current user
            ServerMessage message = new InitServerMessage(session.getId(), userId, field.chunkSize, 0);

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

        scopes.updateEndpointScope(this, Collections.emptySet());
    }

    @OnMessage
    public void onMessage(String message, Session userSession) throws InterruptedException {
        ClientMessage clientMessage = deserializer.actionMessageDataFromJSON(message);
        // TODO: 04.07.2016 split response and broadcast messages (response result code, score and rankings changing should only be send to this message sender)
        ActionServerMessage serverMessage = null;

        EndlessFieldAction action = field.actionInvoker.selectActionByNumber(clientMessage.type);
        Collection<Integer> chunkIds = action.getChunkIds(field, clientMessage.cell);
        Collection<Integer> affectedChunkIds = new HashSet<>();

        field.lockChunksByIds(chunkIds);
        try {
            LinkedHashMap<CellPosition, ? extends EndlessFieldCell> entries = action.perform(field, clientMessage.cell);

            field.updateEntries(entries);

            HashMap<CellPosition, EndlessFieldCellView> cloned = new LinkedHashMap<>(entries.size());
            for (Map.Entry<CellPosition, ? extends EndlessFieldCell> entry : entries.entrySet()) {
                EndlessFieldCell cell = entry.getValue();
                CellPosition position = entry.getKey();

                affectedChunkIds.add(ChunkIdGenerator.chunkIdByPosition(field.chunkSize, position));

//                cloned.put(position, cell.cloneFactory().clone(cell));
                EndlessFieldCellView view = cell.viewFactory().view(cell);
                cloned.put(position, view.cloneFactory().clone(view));
            }

            String userId = ((String) httpSession.getAttribute("user_id"));
            // TODO: 17.06.2016 username from data source
            serverMessage = new ActionServerMessage(cloned, clientMessage.cell, userId, "user #" + userId);
        } finally {
            field.unlockChunks();
        }

        String serialized = serializer.actionEndpointMessageToJSON(serverMessage);

        HashSet<ActionEndpoint> recipients = new HashSet<>();
        recipients.add(this);
        for (Integer chunkId : affectedChunkIds) {
            if (scopes.lockEntry(chunkId)) {
                try {
                    recipients.addAll(scopes.getValue(chunkId));
                } finally {
                    scopes.unlock();
                }
            }
        }

        int c = 0;
        for (ActionEndpoint recipient : recipients) {
            recipient.sendTextMessageAsync(serialized);
            c++;
        }

        System.out.println("message sent to " + c + " endpoints - " + recipients.toString()
                + ", affected chunks size = " + affectedChunkIds.size());
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

    private void internalSendTextMessageAsync(String message) {
        wsSession.getAsyncRemote().sendText(message, sendHandler);
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