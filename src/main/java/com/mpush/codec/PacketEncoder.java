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


import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.util.ByteBuf;

/**
 * Created by ohun on 2016/1/17.
 *
 * @author ohun@live.cn (夜色)
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
