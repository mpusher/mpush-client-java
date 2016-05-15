package com.mpush.handler;

import com.mpush.api.ClientListener;
import com.mpush.api.connection.Connection;
import com.mpush.api.Logger;
import com.mpush.client.ClientConfig;
import com.mpush.api.protocol.Packet;
import com.mpush.message.FastConnectOkMessage;

/**
 * Created by ohun on 2016/1/23.
 */
public final class FastConnectOkHandler extends BaseMessageHandler<FastConnectOkMessage> {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public FastConnectOkMessage decode(Packet packet, Connection connection) {
        return new FastConnectOkMessage(packet, connection);
    }

    @Override
    public void handle(FastConnectOkMessage message) {
        message.getConnection().getSessionContext().setHeartbeat(message.heartbeat);
        ClientListener listener = ClientConfig.I.getClientListener();
        listener.onHandshakeOk(message.getConnection().getClient(), message.heartbeat);
        logger.w("<<< fast connect ok, message=%s", message);
    }
}
