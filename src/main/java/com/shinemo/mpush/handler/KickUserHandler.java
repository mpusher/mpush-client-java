package com.shinemo.mpush.handler;

import com.shinemo.mpush.api.ClientListener;
import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.client.ClientConfig;
import com.shinemo.mpush.message.KickUserMessage;

/**
 * Created by ohun on 2016/1/23.
 */
public final class KickUserHandler extends BaseMessageHandler<KickUserMessage> {
    private Logger logger = ClientConfig.I.getLogger();

    @Override
    public KickUserMessage decode(Packet packet, Connection connection) {
        return new KickUserMessage(packet, connection);
    }

    @Override
    public void handle(KickUserMessage message) {
        logger.w("<<< receive an kickUser message=%s", message);
        ClientListener listener = ClientConfig.I.getClientListener();
        listener.onKickUser(message.deviceId, message.userId);
    }
}
