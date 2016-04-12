package com.zhukovsd.serverapp;

import com.zhukovsd.enrtylockingconcurrenthashmap.Lockable;

import javax.websocket.Session;
import java.util.HashSet;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
public class UserCache extends Lockable{
    // protected by lock
    public HashSet<Session> webSocketSessions = new HashSet<>();
}
