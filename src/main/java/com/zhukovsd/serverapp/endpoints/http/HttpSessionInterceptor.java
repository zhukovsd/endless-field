package com.zhukovsd.serverapp.endpoints.http;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by ZhukovSD on 10.04.2016.
 */
public class HttpSessionInterceptor implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        session.setMaxInactiveInterval(60*60*24); // one day

        System.out.println("session created = " + session.getId());


    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
//        sessions.remove(event.getSession().getId());
    }
}
