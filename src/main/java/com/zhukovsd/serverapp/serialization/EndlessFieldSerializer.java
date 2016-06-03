package com.zhukovsd.serverapp.serialization;

import com.zhukovsd.serverapp.endpoints.http.FieldResponseData;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpointMessage;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ZhukovSD on 03.06.2016.
 */
public interface EndlessFieldSerializer {
    static EndlessFieldSerializer instantiate(String className) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> factoryType = Class.forName(className);
        Constructor<?> constructor = factoryType.getConstructor();
        return (EndlessFieldSerializer) constructor.newInstance();
    }

    void fieldResponseDataToJSON(FieldResponseData data, Appendable out) throws IOException;

    String actionEndpointMessageToJSON(ActionEndpointMessage message);
}
