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

import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.serverapp.cache.sessions.WebSocketSessionsConcurrentHashMap;

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
        // every call from separate thread, same object reused during servlet lifecycle

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
        HttpSession session = event.getSession();
        SessionsCacheConcurrentHashMap sessionsCacheMap = (SessionsCacheConcurrentHashMap) session.getServletContext().getAttribute("sessions_cache");

        String userId = ((String) session.getAttribute("user_id"));
        sessionsCacheMap.remove(userId);
    }
}
