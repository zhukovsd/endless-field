package com.zhukovsd.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public interface Gsonalizer {
    Gson gson = new Gson();

    static String toJson(Gsonable instance) {
        return gson.toJson(instance);
    }

    static String toJson(Gsonable instance, Type instanceType) {
        return gson.toJson(instance, instanceType);
    }

    static void toJson(Gsonable instance, Appendable writer) {
        gson.toJson(instance, writer);
    }

    static void toJson(Gsonable instance, Type instanceType, Appendable writer) {
        gson.toJson(instance, instanceType, writer);
    }

    static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}
