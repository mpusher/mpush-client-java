package com.shinemo.mpush.codec;

import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.ScalableBuffer;

/**
 * Created by ohun on 2016/1/17.
 */
public final class PacketEncoder {

    public static void encode(Packet packet, ScalableBuffer out) {

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
