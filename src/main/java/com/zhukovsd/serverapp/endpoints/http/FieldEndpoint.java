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

package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.endlessfield.ChunkIdGenerator;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.serverapp.cache.scopes.UsersByChunkConcurrentCollection;
import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.cache.sessions.WebSocketSessionsConcurrentHashMap;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpoint;
import com.zhukovsd.serverapp.serialization.EndlessFieldDeserializer;
import com.zhukovsd.serverapp.serialization.EndlessFieldSerializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
// single instance for all requests
@WebServlet(urlPatterns = {"/field"})
public class FieldEndpoint extends HttpServlet {
    private EndlessFieldSerializer getSerializer() {
        return (EndlessFieldSerializer) this.getServletContext().getAttribute("serializer");
    }

    private EndlessFieldDeserializer getDeserializer() {
        return (EndlessFieldDeserializer) this.getServletContext().getAttribute("deserializer");
    }

    private ActionEndpoint getCachedWebSocketEndpoint(String userId, String webSocketSessionId) {
        ActionEndpoint wsEndpoint = null;

        // TODO: 25.04.2016 check if servlet attribute exists
        SessionsCacheConcurrentHashMap sessionsCacheMap = (SessionsCacheConcurrentHashMap)
                getServletContext().getAttribute("sessions_cache");

        WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

        if (webSocketSessionsMap != null) {
            wsEndpoint = webSocketSessionsMap.get(webSocketSessionId);
        }

        return wsEndpoint;
    }

    private UsersByChunkConcurrentCollection getUsersScopesCache() {
        return ((UsersByChunkConcurrentCollection) this.getServletContext().getAttribute("scopes_cache"));
    }

    private EndlessField<?> getField() {
        return (EndlessField) getServletContext().getAttribute("field");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        EndlessFieldSerializer serializer = getSerializer();
        EndlessFieldDeserializer deserializer = getDeserializer();

        // TODO: 29.04.2016 remove null
        FieldResponseData responseData = null;
        boolean isResponseSent = false;

        try {
            // don't create new session on this request. if now session found, report error to the client
            HttpSession session =  request.getSession(false);

            // get already existing session, don't create new one
            if (session != null) {
                // TODO: 18.04.2016 check if user_id attribute exists
                String userId = ((String) session.getAttribute("user_id"));

                FieldRequestData requestData = deserializer.fieldResponseDataFromJSON(
                        URLDecoder.decode(request.getParameter("data"), "UTF-8")
                );

                // TODO: 25.04.2016 validate params
                if (requestData.wsSessionId.isEmpty())
                    // TODO: 25.04.2016 proper exception type
                    throw new RuntimeException("no ws session id specified");

                if (requestData.scope.isEmpty())
                    // TODO: 25.04.2016 proper exception type
                    throw new RuntimeException("scope can't be empty");

                // TODO: 18.04.2016 check if get param exists
                ActionEndpoint<?> wsEndpoint = getCachedWebSocketEndpoint(userId, requestData.wsSessionId);

                if (wsEndpoint != null) {
                    getUsersScopesCache().updateEndpointScope(wsEndpoint, requestData.scope);

                    EndlessField<?> field = getField();
                    field.lockChunksByIds(requestData.scope);
                    try {
                        // TODO: 16.05.2016 set response code
                        responseData = new FieldResponseData();

                        // before unlocking clone cells of chunk to send
                        for (Integer chunkId : requestData.scope) {
                            ArrayList<? extends EndlessFieldCell> cells = field.getCellsByChunkId(chunkId);
                            ArrayList<EndlessFieldCell> clonedCells = new ArrayList<>(cells.size());

                            for (EndlessFieldCell cell : cells) {
                                clonedCells.add(cell.getFactory().clone(cell));
                            }

                            responseData.addChunk(ChunkIdGenerator.chunkOrigin(field.chunkSize, chunkId), clonedCells);
                        }
                    } finally {
                        field.unlockChunks();
                    }

                    serializer.fieldResponseDataToJSON(responseData, response.getWriter());
                    isResponseSent = true;
                } else {
                    // TODO: 18.04.2016 report no ws session error
                }
            } else {
                // TODO: 12.04.2016 report no http session error
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            // Interrupted Exceptions, JsonSyntaxException (Runtime), Param Validation Exception

            // TODO: 25.04.2016 report exception
        }

        if (!isResponseSent)
            serializer.fieldResponseDataToJSON(responseData, response.getWriter());
    }
}
