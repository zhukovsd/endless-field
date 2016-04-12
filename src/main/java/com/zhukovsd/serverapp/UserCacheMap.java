package com.zhukovsd.serverapp;

import com.zhukovsd.enrtylockingconcurrenthashmap.EntryLockingConcurrentHashMap;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
public class UserCacheMap extends EntryLockingConcurrentHashMap<String, UserCache> {
    @Override
    protected UserCache instantiateValue(String key) {
        return new UserCache();
    }
}
