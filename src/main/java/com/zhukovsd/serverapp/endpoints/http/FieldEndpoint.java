package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.endlessfield.field.EndlessCellCloneFactory;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.serialization.Gsonalizer;
import com.zhukovsd.serverapp.cache.scopes.UsersByChunkConcurrentCollection;
import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.cache.sessions.WebSocketSessionsConcurrentHashMap;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpoint;
import com.zhukovsd.simplefield.SimpleFieldCell;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
// single servlet object instance for all requests
@WebServlet(urlPatterns = {"/field"})
public class FieldEndpoint extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
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

        // TODO: 29.04.2016 remove null
        FieldResponseData<? extends EndlessFieldCell> responseData = null;
        boolean isResponseSent = false;

        try {
            // don't create new session on this request. if now session found, report error to the client
            HttpSession session =  request.getSession(false);

            // get already existing session, don't create new one
            if (session != null) {
                // TODO: 18.04.2016 check if user_id attribute exists
                String userId = ((String) session.getAttribute("user_id"));

                FieldRequestData requestData = Gsonalizer.fromJson(URLDecoder.decode(request.getParameter("data"), "UTF-8"), FieldRequestData.class);

                // TODO: 25.04.2016 validate params
                if (requestData.wsSessionId.isEmpty())
                    // TODO: 25.04.2016 proper exception type
                    throw new RuntimeException("no ws session id specified");

                if (requestData.scope.isEmpty())
                    // TODO: 25.04.2016 proper exception type
                    throw new RuntimeException("scope can't be empty");

                // TODO: 18.04.2016 check if get param exists
                ActionEndpoint wsEndpoint = getCachedWebSocketEndpoint(userId, requestData.wsSessionId);

                if (wsEndpoint != null) {
                    getUsersScopesCache().updateUserScope(userId, wsEndpoint.scope, requestData.scope);

                    EndlessField<?> field = getField();
                    field.lockChunksByIds(requestData.scope);

//                    long time = System.nanoTime();

                    StringBuilder sb = null;
//                    String s = "";
                    try {
//                        Map<CellPosition, ? extends EndlessFieldCell> cells = field.getEntriesByChunkIds(requestData.scope);
                        ArrayList<? extends EndlessFieldCell> cells = field.getCellsByChunksId(requestData.scope);
                        ArrayList<EndlessFieldCell> clonedCells = new ArrayList<>(cells.size());

                        EndlessCellCloneFactory factory = cells.get(0).getFactory();
                        for (EndlessFieldCell cell : cells) {
                            EndlessFieldCell clonedCell = factory.clone(cell);
                            clonedCells.add(clonedCell);
                        }

                        responseData = new FieldResponseData<>(clonedCells);

//                        response.getWriter().write("data extracted, \"responseCode\":0");
//                        s = Gsonalizer.toJson(responseData);

//                        sb = new StringBuilder(50000);
//                        sb.append("{\"responseCode\":0,\"cells\":[");
//                        int c = 0;
//                        for (EndlessFieldCell cell : responseData.cells) {
//                            if (c != 0)
//                                sb.append(",");
//
//                            sb.append("{");
//
//                            SimpleFieldCell casted = ((SimpleFieldCell) cell);
//                            if (casted.isChecked()) {
//                                sb.append("\"c\":");
//                                sb.append(String.valueOf(((SimpleFieldCell) cell).isChecked()));
//                            }
//
//                            sb.append("}");
//
//                            c++;
//                        }
//                        sb.append("]}");

//                        response.getWriter().append(sb);

                        // serialize/write response before unlock to prevent response content being changed after unlock
//                        Gsonalizer.toJson(responseData, response.getWriter());
                        isResponseSent = true;
                    } finally {
//                        time = (System.nanoTime() - time) / 1000000;
//                        System.out.println(time + "ms");

                        field.unlockChunks();
//                        System.out.println(Thread.activeCount());
                    }

                    sb = new StringBuilder(50000);
                    sb.append("{\"responseCode\":0,\"cells\":[");
                    int c = 0;
                    for (EndlessFieldCell cell : responseData.cells) {
                        if (c != 0)
                            sb.append(",");

                        sb.append("{");

                        SimpleFieldCell casted = ((SimpleFieldCell) cell);
                        if (casted.isChecked()) {
                            sb.append("\"c\":");
                            sb.append(String.valueOf(((SimpleFieldCell) cell).isChecked()));
                        }

                        sb.append("}");

                        c++;
                    }
                    sb.append("]}");

                    response.getWriter().append(sb);
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
           Gsonalizer.toJson(responseData, response.getWriter());

//        response.getWriter().append("hey buddy");
    }
}
