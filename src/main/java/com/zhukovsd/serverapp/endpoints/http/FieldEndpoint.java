package com.zhukovsd.serverapp.endpoints.http;

import com.google.common.reflect.TypeToken;
import com.zhukovsd.Gsonable;
import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
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
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

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
        // TODO: 29.04.2016 remove null
        FieldResponseData<? extends EndlessFieldCell> responseData = null;
        Type responseType = new TypeToken<FieldResponseData<SimpleFieldCell>>() {}.getType();

        try {
            // don't create new session on this request. if now session found, report error to the client
            HttpSession session =  request.getSession(false);

            // get already existing session, don't create new one
            if (session != null) {
                // TODO: 18.04.2016 check if user_id attribute exists
                String userId = ((String) session.getAttribute("user_id"));

                FieldRequestData requestData = Gsonable.fromJson(URLDecoder.decode(request.getParameter("data"), "UTF-8"), FieldRequestData.class);

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
                    try {
                        LinkedHashMap<CellPosition, ? extends EndlessFieldCell> cells = field.getEntriesByChunkIds(requestData.scope);
                        responseData = new FieldResponseData(cells);
                    } finally {
                        field.unlockChunks();
                    }
                } else {
                    // TODO: 18.04.2016 report no ws session error
                }
            } else {
                // TODO: 12.04.2016 report no http session error
            }
        } catch (Exception e) {
            // Interrupted Exceptions, JsonSyntaxException (Runtime), Param Validation Exception

            // TODO: 25.04.2016 report exception
        }

        Gsonable.toJson(responseData, responseType, response.getWriter());
    }
}
