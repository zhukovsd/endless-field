package com.zhukovsd.serverapp;

import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.serverapp.cache.scopes.UsersByChunkConcurrentCollection;
import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldCellFactory;
import com.zhukovsd.simplefield.SimpleFieldDataSource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
// TODO: 12.04.2016 move to more appropriate package
public class ContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute("sessions_cache", new SessionsCacheConcurrentHashMap());
        // TODO: 26.04.2016 get stripes value from config
        event.getServletContext().setAttribute("scopes_cache", new UsersByChunkConcurrentCollection(10000));
        // TODO: 26.04.2016  get field class and params from config
        event.getServletContext().setAttribute(
                "field", new SimpleField(16, new ChunkSize(50, 50), new SimpleFieldDataSource(), new SimpleFieldCellFactory())
        );
    }

    public void contextDestroyed(ServletContextEvent event) {
        // TODO: 13.04.2016 handle context destruction
    }
}
