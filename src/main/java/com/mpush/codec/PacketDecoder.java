/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.codec;


import com.mpush.api.protocol.Packet;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2016/1/17.
 *
 * @author ohun@live.cn (夜色)
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
                return Packet.HB_PACKET;
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
