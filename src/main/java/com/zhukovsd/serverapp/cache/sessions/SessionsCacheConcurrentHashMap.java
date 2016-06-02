package com.zhukovsd.serverapp.cache.sessions;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhukovSD on 13.04.2016.
 */
// map<user id, websocket sessions map>
public class SessionsCacheConcurrentHashMap extends ConcurrentHashMap<String, WebSocketSessionsConcurrentHashMap> {

}
