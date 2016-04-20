package com.zhukovsd.serverapp.cache.scopes;

import com.zhukovsd.enrtylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpoint;

/**
 * Created by ZhukovSD on 18.04.2016.
 */
public class UsersByChunkConcurrentCollection extends EntryLockingConcurrentHashMap<
        Integer, LockableConcurrentHashSetAdapter<ActionEndpoint>>
{
    //
}
