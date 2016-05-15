package com.mpush.message;


import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.util.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2015/12/28.
 */
public final class ErrorMessage extends ByteBufMessage {
    public byte cmd;
    public byte code;
    public String reason;

    public ErrorMessage(byte cmd, Packet message, Connection connection) {
        super(message, connection);
        this.cmd = cmd;
    }

    public ErrorMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuffer body) {
        cmd = decodeByte(body);
        code = decodeByte(body);
        reason = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeByte(body, cmd);
        encodeByte(body, code);
        encodeString(body, reason);
    }

    public static ErrorMessage from(BaseMessage src) {
        return new ErrorMessage(src.packet.cmd, new Packet(Command.ERROR
                , src.packet.sessionId), src.connection);
    }

    public ErrorMessage setReason(String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public void send() {
        super.sendRaw();
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "cmd=" + cmd +
                ", code=" + code +
                ", reason='" + reason + '\'' +
                '}';
    }
}
