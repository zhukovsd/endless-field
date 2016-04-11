package com.zhukovsd.endpoints.http;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhukovSD on 10.04.2016.
 */
public class HttpSessionInterceptor implements HttpSessionListener {
    private static final Map<String, HttpSession> sessions = new HashMap<String, HttpSession>();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        session.setMaxInactiveInterval(60*60*24); // one day
        sessions.put(session.getId(), session);

        System.out.println("session created = " + session.getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        sessions.remove(event.getSession().getId());
    }

//    public static HttpSession find(String sessionId) {
//        return sessions.get(sessionId);
//    }
}
