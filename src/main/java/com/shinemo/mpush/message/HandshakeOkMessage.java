package com.shinemo.mpush.message;


import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.ScalableBuffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by ohun on 2015/12/27.
 */
public final class HandshakeOkMessage extends ByteBufMessage {
    public byte[] serverKey;
    public String serverHost;
    public long serverTime;
    public int heartbeat;
    public String sessionId;
    public long expireTime;

    public HandshakeOkMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuffer body) {
        serverKey = decodeBytes(body);
        serverHost = decodeString(body);
        serverTime = decodeLong(body);
        heartbeat = decodeInt(body);
        sessionId = decodeString(body);
        expireTime = decodeLong(body);
    }

    @Override
    public void encode(ScalableBuffer body) {
        encodeBytes(body, serverKey);
        encodeString(body, serverHost);
        encodeLong(body, serverTime);
        encodeInt(body, heartbeat);
        encodeString(body, sessionId);
        encodeLong(body, expireTime);
    }

    public static HandshakeOkMessage from(BaseMessage src) {
        return new HandshakeOkMessage(src.createResponse(), src.connection);
    }

    public HandshakeOkMessage setServerKey(byte[] serverKey) {
        this.serverKey = serverKey;
        return this;
    }

    public HandshakeOkMessage setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public HandshakeOkMessage setServerTime(long serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public HandshakeOkMessage setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    public HandshakeOkMessage setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public HandshakeOkMessage setExpireTime(long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public String toString() {
        return "HandshakeOkMessage{" +
                "serverKey=" + Arrays.toString(serverKey) +
                ", serverHost='" + serverHost + '\'' +
                ", serverTime=" + serverTime +
                ", heartbeat=" + heartbeat +
                ", sessionId='" + sessionId + '\'' +
                ", expireTime=" + expireTime +
                '}';
    }
}
