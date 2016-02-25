package com.shinemo.mpush.handler;


import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.client.ClientConfig;
import com.shinemo.mpush.message.OkMessage;

/**
 * Created by ohun on 2015/12/30.
 */
public final class OkMessageHandler extends BaseMessageHandler<OkMessage> {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public OkMessage decode(Packet packet, Connection connection) {
        return new OkMessage(packet, connection);
    }

    @Override
    public void handle(OkMessage message) {
        logger.w("receive an ok message=%s", message);
    }
}
