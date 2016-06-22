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

package com.mpush.client;


import com.mpush.util.thread.EventLock;
import com.mpush.util.thread.ExecutorManager;

import java.util.concurrent.Callable;

/**
 * Created by yxx on 2016/6/9.
 *
 * @author ohun@live.cn (夜色)
 */
public class ConnectThread extends Thread {
    private volatile Callable<Boolean> runningTask;
    private volatile boolean runningFlag = true;
    private final EventLock connLock;
    public ConnectThread(EventLock connLock) {
        this.connLock = connLock;
        this.setName(ExecutorManager.START_THREAD_NAME);
        this.start();
    }

    public synchronized void addConnectTask(Callable<Boolean> task) {
        Callable<Boolean> oldTask = runningTask;
        if (oldTask != null) {
            this.interrupt();
        }
        runningTask = task;
        this.notify();
    }

    public synchronized void shutdown() {
        this.runningFlag = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (runningFlag) {
            try {
                synchronized (this) {
                    while (runningTask == null) {
                        this.wait();
                    }
                }
                if (runningTask.call()) {
                    break;
                }
            } catch (InterruptedException e) {
                continue;
            } catch (Exception e) {
                ClientConfig.I.getLogger().e(e, "run connect task error");
                break;
            }
        }
        //connLock.broadcast();
    }
}
