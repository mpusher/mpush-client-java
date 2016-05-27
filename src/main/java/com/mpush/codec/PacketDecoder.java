package com.mpush.codec;

import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2016/1/17.
 */
public final class PacketDecoder {

    public static Packet decode(ByteBuffer in) {
        Packet hp = decodeHeartbeat(in);
        if (hp != null) return hp;
        return decodeFrame(in);
    }

    private static Packet decodeHeartbeat(ByteBuffer in) {
        if (in.hasRemaining()) {
            in.mark();
            if (in.get() == Packet.HB_PACKET_BYTE) {
                return new Packet(Command.HEARTBEAT);
            }
            in.reset();
        }
        return null;
    }

    private static Packet decodeFrame(ByteBuffer in) {
        if (in.remaining() >= Packet.HEADER_LEN) {
            in.mark();
            int bufferSize = in.remaining();
            int bodyLength = in.getInt();
            if (bufferSize >= (bodyLength + Packet.HEADER_LEN)) {
                return readPacket(in, bodyLength);
            }
            in.reset();
        }
        return null;
    }

    private static Packet readPacket(ByteBuffer in, int bodyLength) {
        byte command = in.get();
        short cc = in.getShort();
        byte flags = in.get();
        int sessionId = in.getInt();
        byte lrc = in.get();
        byte[] body = null;
        if (bodyLength > 0) {
            body = new byte[bodyLength];
            in.get(body);
        }
        Packet packet = new Packet(command);
        packet.cc = cc;
        packet.flags = flags;
        packet.sessionId = sessionId;
        packet.lrc = lrc;
        packet.body = body;
        return packet;
    }
}
