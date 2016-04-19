package com.zhukovsd.enrtylockingconcurrenthashmap;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
public interface Lockable {
    AtomicInteger lockCount = new AtomicInteger(), unlockCount = new AtomicInteger();

    ReentrantLock getLock();
}
