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

package com.mpush.message;


import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.util.ByteBuf;
import com.mpush.api.Constants;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2015/12/28.
 *
 * @author ohun@live.cn (夜色)
 */
public abstract class ByteBufMessage extends BaseMessage {

    public ByteBufMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    protected void decode(byte[] body) {
        decode(ByteBuffer.wrap(body));
    }

    @Override
    protected byte[] encode() {
        ByteBuf body = ByteBuf.allocate(1024);
        encode(body);
        return body.getArray();
    }

    protected abstract void decode(ByteBuffer body);

    protected abstract void encode(ByteBuf body);

    protected void encodeString(ByteBuf body, String field) {
        encodeBytes(body, field == null ? null : field.getBytes(Constants.UTF_8));
    }

    protected void encodeByte(ByteBuf body, byte field) {
        body.put(field);
    }

    protected void encodeInt(ByteBuf body, int field) {
        body.putInt(field);
    }

    protected void encodeLong(ByteBuf body, long field) {
        body.putLong(field);
    }

    protected void encodeBytes(ByteBuf body, byte[] field) {
        if (field == null || field.length == 0) {
            body.putShort(0);
        } else if (field.length < Short.MAX_VALUE) {
            body.putShort(field.length).put(field);
        } else {
            body.putShort(Short.MAX_VALUE).putInt(field.length - Short.MAX_VALUE).put(field);
        }
    }

    protected String decodeString(ByteBuffer body) {
        byte[] bytes = decodeBytes(body);
        if (bytes == null) return null;
        return new String(bytes, Constants.UTF_8);
    }

    protected byte[] decodeBytes(ByteBuffer body) {
        int fieldLength = body.getShort();
        if (fieldLength == 0) return null;
        if (fieldLength == Short.MAX_VALUE) {
            fieldLength += body.getInt();
        }
        byte[] bytes = new byte[fieldLength];
        body.get(bytes);
        return bytes;
    }

    protected byte decodeByte(ByteBuffer body) {
        return body.get();
    }

    protected int decodeInt(ByteBuffer body) {
        return body.getInt();
    }

    protected long decodeLong(ByteBuffer body) {
        return body.getLong();
    }
}
