package com.zhukovsd.serverapp.endpoints.http;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ZhukovSD on 25.04.2016.
 */
public class FieldRequestData {
    String wsSessionId = "";
    HashSet<Integer> scope = new HashSet<>();

    public FieldRequestData(String wsSessionId, HashSet<Integer> scope) {
        this.wsSessionId = wsSessionId;
        this.scope = scope;
    }
}
