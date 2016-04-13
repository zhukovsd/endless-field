package com.zhukovsd.serverapp;

import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpoint;

import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhukovSD on 13.04.2016.
 */
public class WebSocketSessionsConcurrentHashMap extends ConcurrentHashMap<String, ActionEndpoint> {

}
