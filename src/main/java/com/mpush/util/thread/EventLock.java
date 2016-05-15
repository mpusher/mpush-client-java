package com.mpush.util.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ohun on 2016/1/17.
 */
public final class EventLock {
    private final ReentrantLock lock;
    private final Condition cond;

    public EventLock() {
        lock = new ReentrantLock();
        cond = lock.newCondition();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void signal() {
        cond.signal();
    }

    public void signalAll() {
        cond.signalAll();
    }

    public boolean await(long timeout) {
        lock.lock();
        try {
            cond.awaitNanos(TimeUnit.MILLISECONDS.toNanos(timeout));
        } catch (InterruptedException e) {
            return true;
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean await() {
        lock.lock();
        try {
            cond.await();
        } catch (InterruptedException e) {
            return true;
        } finally {
            lock.unlock();
        }
        return false;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Condition getCond() {
        return cond;
    }
}
