package com.zhukovsd.enrtylockingconcurrenthashmap;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
public interface Lockable {
//    ReentrantLock lock = new ReentrantLock();

    void lockInterruptibly() throws InterruptedException;
    void unlock();

    boolean isLocked();

    String test();
}
