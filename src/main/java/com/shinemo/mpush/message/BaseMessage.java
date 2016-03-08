package com.shinemo.mpush.message;


import com.shinemo.mpush.client.ClientConfig;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.Message;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.IOUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ohun on 2015/12/28.
 */
public abstract class BaseMessage implements Message {
    private static final AtomicInteger SID_SEQ = new AtomicInteger();
    protected final Packet packet;
    protected final Connection connection;

    public BaseMessage(Packet packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
        this.decodeBody();
    }

    protected void decodeBody() {
        if (packet.body != null && packet.body.length > 0) {
            //1.解密
            byte[] tmp = packet.body;
            if (packet.hasFlag(Packet.FLAG_CRYPTO)) {
                if (connection.getSessionContext().cipher != null) {
                    tmp = connection.getSessionContext().cipher.decrypt(tmp);
                }
            }

            //2.解压
            if (packet.hasFlag(Packet.FLAG_COMPRESS)) {
                tmp = IOUtils.uncompress(tmp);
            }

            if (tmp.length == 0) {
                throw new RuntimeException("message decode ex");
            }

            packet.body = tmp;
            decode(packet.body);
        }
    }

    protected void encodeBody() {
        byte[] tmp = encode();
        if (tmp != null && tmp.length > 0) {
            //1.压缩
            if (tmp.length > ClientConfig.I.getCompressLimit()) {
                byte[] result = IOUtils.compress(tmp);
                if (result.length > 0) {
                    tmp = result;
                    packet.setFlag(Packet.FLAG_COMPRESS);
                }
            }

            //2.加密
            SessionContext context = connection.getSessionContext();
            if (context.cipher != null) {
                byte[] result = context.cipher.encrypt(tmp);
                if (result.length > 0) {
                    tmp = result;
                    packet.setFlag(Packet.FLAG_CRYPTO);
                }
            }
            packet.body = tmp;
        }
    }

    protected abstract void decode(byte[] body);

    protected abstract byte[] encode();

    public Packet createResponse() {
        return new Packet(packet.cmd, packet.sessionId);
    }

    @Override
    public Packet getPacket() {
        return packet;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void send() {
        encodeBody();
        connection.send(packet);
    }

    @Override
    public void sendRaw() {
        packet.body = encode();
        connection.send(packet);
    }

    protected static int genSessionId() {
        return SID_SEQ.incrementAndGet();
    }

    public int getSessionId() {
        return packet.sessionId;
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "packet=" + packet +
                ", connection=" + connection +
                '}';
    }
}
