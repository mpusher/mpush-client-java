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


import com.mpush.api.Client;
import com.mpush.api.ClientListener;
import com.mpush.util.thread.ExecutorManager;

import java.util.concurrent.Executor;

/*package*/ final class DefaultClientListener implements ClientListener {
    private final Executor executor = ExecutorManager.INSTANCE.getDispatchThread();
    private ClientListener listener;

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    @Override
    public void onConnected(final Client client) {
        if (listener != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onConnected(client);
                }
            });
        }
        client.fastConnect();
    }

    @Override
    public void onDisConnected(final Client client) {
        if (listener != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onDisConnected(client);
                }
            });
        }
    }

    @Override
    public void onHandshakeOk(final Client client, final int heartbeat) {
        if (listener != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onHandshakeOk(client, heartbeat);
                }
            });
        } else {
            //do heathCheck
        }
        client.bindUser(ClientConfig.I.getUserId());
    }

    @Override
    public void onReceivePush(final Client client, final byte[] content, int messageId) {
        if (listener != null) {
            listener.onReceivePush(client, content, messageId);
        }
    }

    @Override
    public void onKickUser(String deviceId, String userId) {
        if (listener != null) {
            listener.onKickUser(deviceId, userId);
        }
    }
}