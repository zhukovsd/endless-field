package com.zhukovsd.serverapp;

import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpoint;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhukovSD on 18.04.2016.
 */
public class UsersScopeConcurrentHashMap extends ConcurrentHashMap<Integer, Set<ActionEndpoint>> {

}
