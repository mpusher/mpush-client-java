package com.shinemo.mpush.api;

/**
 * Created by ohun on 2016/1/23.
 */
public interface ClientListener {
    void onConnected(Client client);

    void onDisConnected(Client client);

    void onHandshakeOk(Client client, int heartbeat);

    void onReceivePush(Client client, String content);

    void onKickUser(String deviceId, String userId);
}
