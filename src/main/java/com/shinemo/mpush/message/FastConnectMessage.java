package com.shinemo.mpush.message;


import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.ScalableBuffer;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2015/12/25.
 */
public final class FastConnectMessage extends ByteBufMessage {
    public String sessionId;
    public String deviceId;
    public int minHeartbeat;
    public int maxHeartbeat;

    public FastConnectMessage(Connection connection) {
        super(new Packet(Command.FAST_CONNECT, genSessionId()), connection);
    }

    public FastConnectMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuffer body) {
        sessionId = decodeString(body);
        deviceId = decodeString(body);
        minHeartbeat = decodeInt(body);
        maxHeartbeat = decodeInt(body);
    }

    @Override
    public void encode(ScalableBuffer body) {
        encodeString(body, sessionId);
        encodeString(body, deviceId);
        encodeInt(body, minHeartbeat);
        encodeInt(body, maxHeartbeat);
    }

    @Override
    public String toString() {
        return "FastConnectMessage{" +
                "sessionId='" + sessionId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", minHeartbeat=" + minHeartbeat +
                ", maxHeartbeat=" + maxHeartbeat +
                '}';
    }
}
