package com.mpush.message;

import java.nio.ByteBuffer;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.util.ByteBuf;


public class ChatMessage extends ByteBufMessage{

	public String destUserId;
	public String content;
	
	public ChatMessage(Packet packet, Connection connection) {
		super(packet, connection);
	}
	
	public ChatMessage(String destUserId, String content, Connection connection) {
		super(new Packet(Command.CHAT, genSessionId()), connection);
	}

	@Override
	public void decode(ByteBuffer body) {
		destUserId = decodeString(body);
		content = decodeString(body);
	}

	@Override
	public void encode(ByteBuf body) {
		encodeString(body, destUserId);
        encodeString(body, content);
	}

}
