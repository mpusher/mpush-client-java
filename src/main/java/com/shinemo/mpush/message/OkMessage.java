package com.shinemo.mpush.message;


import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.WrappedByteBuffer;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2015/12/28.
 */
public final class OkMessage extends ByteBufMessage {
    public byte cmd;
    public byte code;
    public String data;

    public OkMessage(byte cmd, Packet message, Connection connection) {
        super(message, connection);
        this.cmd = cmd;
    }

    public OkMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuffer body) {
        cmd = decodeByte(body);
        code = decodeByte(body);
        data = decodeString(body);
    }

    @Override
    public void encode(WrappedByteBuffer body) {
        encodeByte(body, cmd);
        encodeByte(body, code);
        encodeString(body, data);
    }

    public static OkMessage from(BaseMessage src) {
        return new OkMessage(src.packet.cmd, new Packet(Command.OK
                , src.packet.sessionId), src.connection);
    }

    public OkMessage setCode(byte code) {
        this.code = code;
        return this;
    }

    public OkMessage setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "OkMessage{" +
                "cmd=" + cmd +
                ", code=" + code +
                ", data='" + data + '\'' +
                '}';
    }
}
