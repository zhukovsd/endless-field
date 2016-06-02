package com.zhukovsd.endlessfield.field;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ZhukovSD on 21.03.2016.
 */
public interface EndlessFieldCellFactory<T extends EndlessFieldCell> {
    T create();

    static EndlessFieldCellFactory instantiate(String className) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> factoryType = Class.forName(className);
        Constructor<?> constructor = factoryType.getConstructor();
        return (EndlessFieldCellFactory) constructor.newInstance();
    }
}
