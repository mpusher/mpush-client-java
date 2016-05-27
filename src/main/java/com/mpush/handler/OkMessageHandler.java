package com.mpush.handler;


import com.mpush.api.Logger;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.message.OkMessage;
import com.mpush.util.DefaultLogger;

/**
 * Created by ohun on 2015/12/30.
 */
public final class OkMessageHandler extends BaseMessageHandler<OkMessage> {
	private static final Logger logger = new DefaultLogger(OkMessageHandler.class);

    @Override
    public OkMessage decode(Packet packet, Connection connection) {
        return new OkMessage(packet, connection);
    }

    @Override
    public void handle(OkMessage message) {
        logger.d("<<< receive an ok message = %s", message);
        if(message.cmd == Command.CHAT.cmd)
        	message.getConnection().getSessionContext().getCallBack().onSuccess(message.data, "");
    }
}
