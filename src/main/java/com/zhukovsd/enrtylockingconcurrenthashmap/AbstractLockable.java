package com.zhukovsd.enrtylockingconcurrenthashmap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ZhukovSD on 18.04.2016.
 */
public class AbstractLockable implements Lockable {
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public ReentrantLock getLock() {
        return lock;
    }
}
