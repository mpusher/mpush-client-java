package com.shinemo.mpush.message;


import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/30.
 */
public final class PushMessage extends BaseMessage {

    public String content;

    public PushMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }


    @Override
    public void decode(byte[] body) {
        content = new String(body, Constants.UTF_8);
    }

    @Override
    public byte[] encode() {
        return content == null ? null : content.getBytes(Constants.UTF_8);
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "content='" + content + '\'' +
                '}';
    }
}
