package com.zhukovsd.serverapp.serialization;

import com.google.gson.Gson;
import com.zhukovsd.serverapp.endpoints.http.FieldResponseData;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpointMessage;

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
    public String actionEndpointMessageToJSON(ActionEndpointMessage message) {
        return gson.toJson(message);
    }
}
