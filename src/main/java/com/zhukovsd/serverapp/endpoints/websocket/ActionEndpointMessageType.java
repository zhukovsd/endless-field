package com.zhukovsd.serverapp.endpoints.websocket;

/**
 * Created by ZhukovSD on 05.05.2016.
 */
public enum ActionEndpointMessageType {
    INIT_MESSAGE (0),
    ACTION_MESSAGE (1);

    public final int value;

    ActionEndpointMessageType(int value) {
        this.value = value;
    }
}
