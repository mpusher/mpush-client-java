package com.shinemo.mpush.message;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.ScalableBuffer;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2015/12/28.
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
        ScalableBuffer body = ScalableBuffer.allocate(1024);
        encode(body);
        return body.getArray();
    }

    protected abstract void decode(ByteBuffer body);

    protected abstract void encode(ScalableBuffer body);

    protected void encodeString(ScalableBuffer body, String field) {
        encodeBytes(body, field == null ? null : field.getBytes(Constants.UTF_8));
    }

    protected void encodeByte(ScalableBuffer body, byte field) {
        body.put(field);
    }

    protected void encodeInt(ScalableBuffer body, int field) {
        body.putInt(field);
    }

    protected void encodeLong(ScalableBuffer body, long field) {
        body.putLong(field);
    }

    protected void encodeBytes(ScalableBuffer body, byte[] field) {
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
