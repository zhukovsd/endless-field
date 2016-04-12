package com.zhukovsd.serverapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.zhukovsd.serverapp.UserCacheMap;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
// TODO: 12.04.2016 move to more appropriate package
public class ContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
        try {
            event.getServletContext().setAttribute("usercache", new UserCacheMap());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println("123");
    }

    public void contextDestroyed(ServletContextEvent event) {

    }
}
