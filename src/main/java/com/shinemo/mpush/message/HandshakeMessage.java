package com.shinemo.mpush.message;


import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.ScalableBuffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by ohun on 2015/12/24.
 */
public final class HandshakeMessage extends ByteBufMessage {
    public String deviceId;
    public String osName;
    public String osVersion;
    public String clientVersion;
    public byte[] iv;
    public byte[] clientKey;
    public int minHeartbeat;
    public int maxHeartbeat;
    public long timestamp;

    public HandshakeMessage(Connection connection) {
        super(new Packet(Command.HANDSHAKE, genSessionId()), connection);
    }

    public HandshakeMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    protected void decode(ByteBuffer body) {
        deviceId = decodeString(body);
        osName = decodeString(body);
        osVersion = decodeString(body);
        clientVersion = decodeString(body);
        iv = decodeBytes(body);
        clientKey = decodeBytes(body);
        minHeartbeat = decodeInt(body);
        maxHeartbeat = decodeInt(body);
        timestamp = decodeLong(body);
    }

    protected void encode(ScalableBuffer body) {
        encodeString(body, deviceId);
        encodeString(body, osName);
        encodeString(body, osVersion);
        encodeString(body, clientVersion);
        encodeBytes(body, iv);
        encodeBytes(body, clientKey);
        encodeInt(body, minHeartbeat);
        encodeInt(body, maxHeartbeat);
        encodeLong(body, timestamp);
    }

    @Override
    public String toString() {
        return "HandshakeMessage{" +
                "deviceId='" + deviceId + '\'' +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", iv=" + Arrays.toString(iv) +
                ", clientKey=" + Arrays.toString(clientKey) +
                ", minHeartbeat=" + minHeartbeat +
                ", maxHeartbeat=" + maxHeartbeat +
                ", timestamp=" + timestamp +
                '}';
    }
}
