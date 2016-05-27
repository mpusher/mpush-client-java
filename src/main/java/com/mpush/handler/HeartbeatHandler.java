package com.mpush.handler;


import com.mpush.api.Logger;
import com.mpush.api.MessageHandler;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.util.DefaultLogger;

/**
 * Created by ohun on 2015/12/30.
 */
public final class HeartbeatHandler implements MessageHandler {
	private static final Logger logger = new DefaultLogger(HeartbeatHandler.class);

    @Override
    public void handle(Packet packet, Connection connection) {
        logger.d("<<< receive heartbeat pong...");
    }
}
