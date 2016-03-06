package com.shinemo.mpush.handler;


import com.shinemo.mpush.client.ClientConfig;
import com.shinemo.mpush.api.ClientListener;
import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.message.PushMessage;

/**
 * Created by ohun on 2015/12/30.
 */
public final class PushMessageHandler extends BaseMessageHandler<PushMessage> {
    private final Logger logger = ClientConfig.I.getLogger();
    private final ClientListener listener = ClientConfig.I.getClientListener();

    @Override
    public PushMessage decode(Packet packet, Connection connection) {
        return new PushMessage(packet, connection);
    }

    @Override
    public void handle(PushMessage message) {
        logger.d("<<< receive a push message=%s", message.content);
        listener.onReceivePush(message.getConnection().getClient(), message.content);
    }
}
