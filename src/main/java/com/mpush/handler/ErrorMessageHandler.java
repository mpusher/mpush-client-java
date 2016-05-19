package com.mpush.handler;

import com.mpush.api.Logger;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.client.ClientConfig;
import com.mpush.message.ErrorMessage;
import com.mpush.util.DefaultLogger;

/**
 * Created by ohun on 2015/12/30.
 */
public final class ErrorMessageHandler extends BaseMessageHandler<ErrorMessage> {
	private static final Logger logger = new DefaultLogger(ErrorMessageHandler.class);

	@Override
	public ErrorMessage decode(Packet packet, Connection connection) {
		return new ErrorMessage(packet, connection);
	}

	@Override
	public void handle(ErrorMessage message) {
		logger.d("<<< receive an error message=%s", message);
		if (message.cmd == Command.FAST_CONNECT.cmd) {
			ClientConfig.I.getSessionStorage().clearSession();
			message.getConnection().getClient().handshake();
		} else if (message.cmd == Command.HANDSHAKE.cmd) {
			message.getConnection().getClient().stop();
		} else if (message.cmd == Command.CHAT.cmd) {
			switch (message.code) {
			case 1:
				// user offline
				message.getConnection().getSessionContext().getCallBack()
						.onOffline(message.getConnection().getSessionContext().getUserId());
				break;
			case 2:
				// push to client failure
				message.getConnection().getSessionContext().getCallBack()
						.onFailure(message.getConnection().getSessionContext().getUserId());
				break;
			case 3:
				// router change
				break;
			case 100:
				// handle message error
				break;
			default:
				break;
			}
		} else {
			message.getConnection().reconnect();
		}
	}
}
