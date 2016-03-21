package com.zhukovsd;

import com.google.gson.Gson;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public interface Gsonable {
    Gson gson = new Gson();

    static String toJson(Object instance) {
        return gson.toJson(instance);
    }

    static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}
