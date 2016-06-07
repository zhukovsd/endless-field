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

import com.google.gson.Gson;
import com.zhukovsd.serverapp.endpoints.http.FieldResponseData;
import com.zhukovsd.serverapp.endpoints.websocket.ActionServerMessage;
import com.zhukovsd.serverapp.endpoints.websocket.ServerMessage;

import java.io.IOException;

/**
 * Created by ZhukovSD on 03.06.2016.
 */
public class EndlessFieldGsonSerializer implements EndlessFieldSerializer {
    private Gson gson = new Gson();

    @Override
    public void fieldResponseDataToJSON(FieldResponseData data, Appendable out) throws IOException {
        gson.toJson(data, out);
    }

    @Override
    public String actionEndpointMessageToJSON(ServerMessage message) {
        return gson.toJson(message);
    }
}
