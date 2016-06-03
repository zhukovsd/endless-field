package com.zhukovsd.serverapp.endpoints.websocket;

/**
 * Created by ZhukovSD on 05.05.2016.
 */
public class ActionEndpointMessage {
    public final int type;

    public ActionEndpointMessage(ActionEndpointMessageType type) {
        this.type = type.value;
    }
}
