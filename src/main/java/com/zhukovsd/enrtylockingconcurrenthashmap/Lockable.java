package com.zhukovsd.enrtylockingconcurrenthashmap;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ZhukovSD on 12.04.2016.
 */
public abstract class Lockable {
    ReentrantLock lock = new ReentrantLock();
}
