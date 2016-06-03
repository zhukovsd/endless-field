package com.zhukovsd.serverapp.serialization;

import com.zhukovsd.serverapp.endpoints.http.FieldRequestData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ZhukovSD on 03.06.2016.
 */
public interface EndlessFieldDeserializer {
    static EndlessFieldDeserializer instantiate(String className) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> factoryType = Class.forName(className);
        Constructor<?> constructor = factoryType.getConstructor();
        return (EndlessFieldDeserializer) constructor.newInstance();
    }

    FieldRequestData fieldResponseDataFromJSON(String string);
}
