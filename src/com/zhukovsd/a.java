package com.zhukovsd;

import com.google.gson.Gson;

interface Jsonable {
    Gson gson = new Gson();

    static String toJson(Object instance) {
        return gson.toJson(instance);
    }

    static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}

/**
 * Created by ZhukovSD on 12.03.2016.
 */
public class A implements Jsonable {
    int number = 1;

    public static void main(String[] args) {
        A a = new A();
        a.number = 2;

        String s = Jsonable.toJson(a);
        System.out.println(s);

        A aa = Jsonable.fromJson(s, A.class);

        s.replace(new StringBuffer("a"), new StringBuffer("b"));
    }
}
