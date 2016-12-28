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


import com.mpush.client.ClientConfig;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 2016/1/23.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ExecutorManager {
    public static final String THREAD_NAME_PREFIX = "mp-client-";
    public static final String WRITE_THREAD_NAME = THREAD_NAME_PREFIX + "write-t";
    public static final String READ_THREAD_NAME = THREAD_NAME_PREFIX + "read-t";
    public static final String DISPATCH_THREAD_NAME = THREAD_NAME_PREFIX + "dispatch-t";
    public static final String START_THREAD_NAME = THREAD_NAME_PREFIX + "start-t";
    public static final String TIMER_THREAD_NAME = THREAD_NAME_PREFIX + "timer-t";
    public static final ExecutorManager INSTANCE = new ExecutorManager();
    private ThreadPoolExecutor writeThread;
    private ThreadPoolExecutor dispatchThread;
    private ScheduledExecutorService timerThread;

    public ThreadPoolExecutor getWriteThread() {
        if (writeThread == null || writeThread.isShutdown()) {
            writeThread = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(100),
                    new NamedThreadFactory(WRITE_THREAD_NAME),
                    new RejectedHandler());
        }
        return writeThread;
    }

    public ThreadPoolExecutor getDispatchThread() {
        if (dispatchThread == null || dispatchThread.isShutdown()) {
            dispatchThread = new ThreadPoolExecutor(2, 4,
                    1L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(100),
                    new NamedThreadFactory(DISPATCH_THREAD_NAME),
                    new RejectedHandler());
        }
        return dispatchThread;
    }

    public ScheduledExecutorService getTimerThread() {
        if (timerThread == null || timerThread.isShutdown()) {
            timerThread = new ScheduledThreadPoolExecutor(1,
                    new NamedThreadFactory(TIMER_THREAD_NAME),
                    new RejectedHandler());
        }
        return timerThread;
    }

    public synchronized void shutdown() {
        if (writeThread != null) {
            writeThread.shutdownNow();
            writeThread = null;
        }
        if (dispatchThread != null) {
            dispatchThread.shutdownNow();
            dispatchThread = null;
        }
        if (timerThread != null) {
            timerThread.shutdownNow();
            timerThread = null;
        }
    }

    public static boolean isMPThread() {
        return Thread.currentThread().getName().startsWith(THREAD_NAME_PREFIX);
    }

    private static class RejectedHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            ClientConfig.I.getLogger().w("a task was rejected r=%s", r);
        }
    }
}
