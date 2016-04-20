package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.cache.sessions.WebSocketSessionsConcurrentHashMap;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpoint;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

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

    private ActionEndpoint getCachedWebSocketEndpoint(HttpSession session, String webSocketSessionId) {
        ActionEndpoint wsEndpoint = null;

        SessionsCacheConcurrentHashMap sessionsCacheMap = (SessionsCacheConcurrentHashMap) this.
                getServletContext().getAttribute("sessions_cache");

        // TODO: 18.04.2016 check if attribute exists
        String userId = ((String) session.getAttribute("user_id"));

        WebSocketSessionsConcurrentHashMap webSocketSessionsMap = sessionsCacheMap.get(userId);

        if (webSocketSessionsMap != null) {
            wsEndpoint = webSocketSessionsMap.get(userId);
        }

        return wsEndpoint;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // don't create new session on this request. if now session found, report error to the client
        HttpSession session = request.getSession(false);

        if (session != null) {
//            response.getOutputStream().print("session = " + session.getId());

            // TODO: 18.04.2016 check if get param exists
            ActionEndpoint wsEndpoint = getCachedWebSocketEndpoint(session, request.getParameter("ws_session_id"));

            if (wsEndpoint != null) {
                // idiom of thread-safe working with scopes map showed in experiments/concurrency/ConcurrentUserScopesExperiment.java

                wsEndpoint.scope.clear();
                // set wsEndpoint.scope from request param
//                wsEndpoint.scope.add()

                // update clients scope map by iterating current scope
            } else {
                // TODO: 18.04.2016 report no ws session error
            }
        } else {
            // TODO: 12.04.2016 report no http session error
        }
    }
}
