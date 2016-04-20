package com.zhukovsd.serverapp;

import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
// TODO: 12.04.2016 move to more appropriate package
public class ContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute("sessions_cache", new SessionsCacheConcurrentHashMap());
    }

    public void contextDestroyed(ServletContextEvent event) {
        // TODO: 13.04.2016 handle context destruction
    }
}
