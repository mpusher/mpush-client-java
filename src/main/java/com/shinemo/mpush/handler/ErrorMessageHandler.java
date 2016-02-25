package com.shinemo.mpush.handler;


import com.shinemo.mpush.client.ClientConfig;
import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.message.ErrorMessage;

/**
 * Created by ohun on 2015/12/30.
 */
public final class ErrorMessageHandler extends BaseMessageHandler<ErrorMessage> {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public ErrorMessage decode(Packet packet, Connection connection) {
        return new ErrorMessage(packet, connection);
    }

    @Override
    public void handle(ErrorMessage message) {
        logger.w("receive an error message=%s", message);
        if (message.cmd == Command.FAST_CONNECT.cmd) {
            ClientConfig.I.getSessionStorage().clearSession();
            message.getConnection().getClient().handshake();
        } else {
            message.getConnection().reconnect();
        }
    }
}
