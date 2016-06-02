package com.zhukovsd.serverapp.cache.sessions;

import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpoint;

import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhukovSD on 13.04.2016.
 */
// map<websocket session id, action websocket endpoint instance>
public class WebSocketSessionsConcurrentHashMap extends ConcurrentHashMap<String, ActionEndpoint> {

}
