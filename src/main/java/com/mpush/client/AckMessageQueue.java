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


import com.mpush.api.Logger;
import com.mpush.api.http.HttpCallback;
import com.mpush.api.http.HttpRequest;
import com.mpush.api.http.HttpResponse;
import com.mpush.api.push.AckModel;
import com.mpush.api.push.PushCallback;
import com.mpush.api.push.PushContext;
import com.mpush.util.thread.ExecutorManager;

import java.util.Map;
import java.util.concurrent.*;

import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;

/**
 * Created by yxx on 2016/2/16.
 *
 * @author ohun@live.cn
 */
public final class AckMessageQueue {
    private final Map<Integer, PushTask> queue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timer = ExecutorManager.INSTANCE.getHttpRequestThread();
    private final Callable<Boolean> NONE = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return Boolean.FALSE;
        }
    };

    private final Logger logger = ClientConfig.I.getLogger();

    public Future<Boolean> add(int sessionId, PushContext context) {
        if (context.ackModel == AckModel.NO_ACK) return null;
        if (context.callback == null) return null;
        PushTask task = new PushTask(sessionId, context);
        queue.put(sessionId, task);
        task.future = timer.schedule(task, task.timeout, TimeUnit.MILLISECONDS);
        return task;
    }

    public PushTask getAndRemove(int sessionId) {
        return queue.remove(sessionId);
    }

    public final class PushTask extends FutureTask<Boolean> implements Runnable {
        private PushCallback callback;
        private final int timeout;
        private final long sendTime;
        private final int sessionId;
        private Future<?> future;

        private PushTask(int sessionId, PushContext context) {
            super(NONE);
            this.callback = context.getCallback();
            this.timeout = context.getTimeout();
            this.sendTime = System.currentTimeMillis();
            this.sessionId = sessionId;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run() {
            queue.remove(sessionId);
            timeout();
        }

        public void timeout() {
            call(false);
        }

        public void success() {
            call(true);
        }

        private void call(boolean success) {
            if (this.future.cancel(true)) {
                this.set(success);
                if (callback != null) {
                    if (success) callback.onSuccess();
                    else callback.onTimeout();
                }
                callback = null;
            }
            logger.d("one push task end, sessionId=%d, costTime=%d, success=%b",
                    sessionId, (System.currentTimeMillis() - sendTime), success);
        }
    }
}
