/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.util.thread;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ohun on 2016/1/17.
 *
 * @author ohun@live.cn (夜色)
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

    public void broadcast() {
        lock.lock();
        cond.signalAll();
        lock.unlock();
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
