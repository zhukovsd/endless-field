package com.zhukovsd.serverapp;

import com.zhukovsd.endlessfield.field.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldCellFactory;
import com.zhukovsd.endlessfield.fielddatasource.EndlessFieldDataSource;
import com.zhukovsd.serverapp.serialization.EndlessFieldDeserializer;
import com.zhukovsd.serverapp.serialization.EndlessFieldSerializer;
import com.zhukovsd.serverapp.cache.scopes.UsersByChunkConcurrentCollection;
import com.zhukovsd.serverapp.cache.sessions.SessionsCacheConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
// TODO: 12.04.2016 move to more appropriate package
public class ContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();

        context.setAttribute("sessions_cache", new SessionsCacheConcurrentHashMap());

        context.setAttribute(
                "scopes_cache",
                new UsersByChunkConcurrentCollection(
                        Integer.parseInt(context.getInitParameter("ChunkMapStripesCount"))
                )
        );

        try {
            context.setAttribute(
                    "serializer",
                    EndlessFieldSerializer.instantiate(context.getInitParameter("EndlessFieldSerializerClassName"))
            );

            context.setAttribute(
                    "deserializer",
                    EndlessFieldDeserializer.instantiate(context.getInitParameter("EndlessFieldDeserializerClassName"))
            );

            String fieldClassName = context.getInitParameter("EndlessFieldClassName");

            int stripes = Integer.parseInt(context.getInitParameter("EndlessFieldStripesCount"));
            ChunkSize chunkSize = new ChunkSize(
                    Integer.parseInt(context.getInitParameter("ChunkRowCount")),
                    Integer.parseInt(context.getInitParameter("ChunkColumnCount"))
            );

            EndlessField field = EndlessField.instantiate(
                    fieldClassName, stripes, chunkSize,
                    EndlessFieldDataSource.instantiate(context.getInitParameter("EndlessFieldDataSourceClassName")),
                    EndlessFieldCellFactory.instantiate(context.getInitParameter("EndlessFieldCellFactoryClassName"))
            );

            context.setAttribute("field", field);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
             e.printStackTrace();
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        // TODO: 13.04.2016 handle context destruction
    }
}
