package com.zhukovsd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * Created by ZhukovSD on 19.03.2016.
 */
public interface Gsonable {
//    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    Gson gson = new Gson();

    static String toJson(Object instance) {
        return gson.toJson(instance);
    }

    static String toJson(Object instance, Type instanceType) {
        return gson.toJson(instance, instanceType);
    }

    static void toJson(Object instance, Appendable writer) {
        gson.toJson(instance, writer);
    }

    static void toJson(Object instance, Type instanceType, Appendable writer) {
        gson.toJson(instance, instanceType, writer);
    }

    static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}
