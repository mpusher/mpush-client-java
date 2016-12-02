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
import com.mpush.api.ack.AckCallback;
import com.mpush.api.ack.AckContext;
import com.mpush.api.ack.AckModel;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.util.thread.ExecutorManager;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ohun on 2016/11/13.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AckRequestMgr {
    private static AckRequestMgr I;

    private final Logger logger = ClientConfig.I.getLogger();

    private final Map<Integer, RequestTask> queue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timer = ExecutorManager.INSTANCE.getTimerThread();
    private final Callable<Boolean> NONE = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return Boolean.FALSE;
        }
    };
    private Connection connection;


    public static AckRequestMgr I() {
        if (I == null) {
            synchronized (AckRequestMgr.class) {
                if (I == null) {
                    I = new AckRequestMgr();
                }
            }
        }
        return I;
    }

    private AckRequestMgr() {
    }

    public Future<Boolean> add(int sessionId, AckContext context) {
        if (context.ackModel == AckModel.NO_ACK) return null;
        if (context.callback == null) return null;
        return addTask(new RequestTask(sessionId, context));
    }

    public RequestTask getAndRemove(int sessionId) {
        return queue.remove(sessionId);
    }


    public void clear() {
        for (RequestTask task : queue.values()) {
            try {
                task.future.cancel(true);
            } catch (Exception e) {
            }
        }
    }

    private RequestTask addTask(RequestTask task) {
        queue.put(task.sessionId, task);
        task.future = timer.schedule(task, task.timeout, TimeUnit.MILLISECONDS);
        return task;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public final class RequestTask extends FutureTask<Boolean> implements Runnable {
        private final int timeout;
        private final long sendTime;
        private final int sessionId;
        private AckCallback callback;
        private Packet request;
        private Future<?> future;
        private int retryCount;

        private RequestTask(AckCallback callback, int timeout, int sessionId, Packet request, int retryCount) {
            super(NONE);
            this.callback = callback;
            this.timeout = timeout;
            this.sendTime = System.currentTimeMillis();
            this.sessionId = sessionId;
            this.request = request;
            this.retryCount = retryCount;
        }

        private RequestTask(int sessionId, AckContext context) {
            this(context.callback, context.timeout, sessionId, context.request, context.retryCount);
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
            call(null);
        }

        public void success(Packet packet) {
            call(packet);
        }

        private void call(Packet response) {
            if (this.future.cancel(true)) {
                boolean success = response != null;
                this.set(success);
                if (callback != null) {
                    if (success) {
                        logger.d("receive one ack response, sessionId=%d, costTime=%d, request=%s, response=%s"
                                , sessionId, (System.currentTimeMillis() - sendTime), request, response
                        );
                        callback.onSuccess(response);
                    } else if (request != null && retryCount > 0) {
                        logger.w("one ack request timeout, retry=%d, sessionId=%d, costTime=%d, request=%s"
                                , retryCount, sessionId, (System.currentTimeMillis() - sendTime), request
                        );
                        addTask(copy(retryCount - 1));
                        connection.send(request);
                    } else {
                        logger.w("one ack request timeout, sessionId=%d, costTime=%d, request=%s"
                                , sessionId, (System.currentTimeMillis() - sendTime), request
                        );
                        callback.onTimeout(request);
                    }
                }
                callback = null;
                request = null;
            }
        }

        private RequestTask copy(int retryCount) {
            return new RequestTask(callback, timeout, sessionId, request, retryCount);
        }
    }
}
