package com.zhukovsd.serverapp.endpoints.websocket;

import com.zhukovsd.serialization.Gsonable;

/**
 * Created by ZhukovSD on 05.05.2016.
 */
public class ActionEndpointMessage implements Gsonable {
    private final int type;

    public ActionEndpointMessage(ActionEndpointMessageType type) {
        this.type = type.value;
    }
}
