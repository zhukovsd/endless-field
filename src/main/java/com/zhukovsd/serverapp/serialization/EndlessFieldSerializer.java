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

package com.zhukovsd.serverapp.serialization;

import com.zhukovsd.serverapp.endpoints.http.FieldResponseData;
import com.zhukovsd.serverapp.endpoints.websocket.ActionServerMessage;

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

    String actionEndpointMessageToJSON(ActionServerMessage message);
}
