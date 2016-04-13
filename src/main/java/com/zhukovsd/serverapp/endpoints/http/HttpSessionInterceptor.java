package com.zhukovsd.serverapp.endpoints.http;

import com.zhukovsd.serverapp.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.WebSocketSessionsConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 10.04.2016.
 */
public class HttpSessionInterceptor implements HttpSessionListener {
    AtomicInteger c = new AtomicInteger();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        // every call is separate thread, same object reused during servlet lifecycle

        HttpSession session = event.getSession();
        session.setMaxInactiveInterval(60*60*24); // one day

        SessionsCacheConcurrentHashMap sessionsCacheMap = (SessionsCacheConcurrentHashMap) session.getServletContext().getAttribute("sessions_cache");

        // put new entry in sessions cache map, if multiple threads will do in simultaneously, only first will create
        // new entry due to putIfAbsent() atomicity
        // TODO: 13.04.2016 proper user_id generation
        String userId = ((Integer) c.getAndIncrement()).toString();

        session.setAttribute("user_id", userId);
        sessionsCacheMap.putIfAbsent(userId, new WebSocketSessionsConcurrentHashMap());

        System.out.println("session created = " + session.getId());
//        System.out.println("this = " + ((Object) this) + ", current thread = " + Thread.currentThread().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
//        sessions.remove(event.getSession().getId());
        // TODO: 13.04.2016 remove entry from sessions cache
    }
}
