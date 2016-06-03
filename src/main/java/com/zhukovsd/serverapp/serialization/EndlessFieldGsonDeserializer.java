package com.zhukovsd.serverapp.serialization;

import com.google.gson.Gson;
import com.zhukovsd.serverapp.endpoints.http.FieldRequestData;

/**
 * Created by ZhukovSD on 03.06.2016.
 */
public class EndlessFieldGsonDeserializer implements EndlessFieldDeserializer {
    private Gson gson = new Gson();

    @Override
    public FieldRequestData fieldResponseDataFromJSON(String string) {
        return gson.fromJson(string, FieldRequestData.class);
    }
}
