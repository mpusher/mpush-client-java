package com.mpush.handler;


import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.api.Logger;
import com.mpush.api.MessageHandler;
import com.mpush.client.ClientConfig;

/**
 * Created by ohun on 2015/12/30.
 */
public final class HeartbeatHandler implements MessageHandler {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public void handle(Packet packet, Connection connection) {
        logger.d("<<< receive heartbeat pong...");
    }
}
