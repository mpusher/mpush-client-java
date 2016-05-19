package com.mpush.handler;


import com.mpush.api.ClientListener;
import com.mpush.api.Logger;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.client.ClientConfig;
import com.mpush.message.PushMessage;
import com.mpush.util.DefaultLogger;

/**
 * Created by ohun on 2015/12/30.
 */
public final class PushMessageHandler extends BaseMessageHandler<PushMessage> {
	private static final Logger logger = new DefaultLogger(PushMessageHandler.class);
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
