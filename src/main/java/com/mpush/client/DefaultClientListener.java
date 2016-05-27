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
    public void onReceivePush(final Client client, final String content) {
        if (listener != null) {
            listener.onReceivePush(client, content);
        }
    }

    @Override
    public void onKickUser(String deviceId, String userId) {
        if (listener != null) {
            listener.onKickUser(deviceId, userId);
        }
    }
}