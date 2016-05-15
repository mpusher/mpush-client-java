package com.mpush.codec;

import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.util.ByteBuf;

/**
 * Created by ohun on 2016/1/17.
 */
public final class PacketEncoder {

    public static void encode(Packet packet, ByteBuf out) {

        if (packet.cmd == Command.HEARTBEAT.cmd) {
            out.put(Packet.HB_PACKET_BYTE);
        } else {
            out.putInt(packet.getBodyLength());
            out.put(packet.cmd);
            out.putShort(packet.cc);
            out.put(packet.flags);
            out.putInt(packet.sessionId);
            out.put(packet.lrc);
            if (packet.getBodyLength() > 0) {
                out.put(packet.body);
            }
        }
    }
}
