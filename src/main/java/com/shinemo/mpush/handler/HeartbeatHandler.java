package com.shinemo.mpush.handler;


import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.client.ClientConfig;
import com.shinemo.mpush.message.OkMessage;

/**
 * Created by ohun on 2015/12/30.
 */
public final class HeartbeatHandler implements MessageHandler {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public void handle(Packet packet, Connection connection) {
        logger.d("receive heartbeat pong...");
    }
}
