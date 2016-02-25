package com.shinemo.mpush.util;

import java.nio.*;

/**
 * Created by ohun on 2016/1/21.
 */
public final class ScalableBuffer {
    private ByteBuffer tmpNioBuf;

    public static ScalableBuffer allocate(int capacity) {
        ScalableBuffer buffer = new ScalableBuffer();
        buffer.tmpNioBuf = ByteBuffer.allocate(capacity);
        return buffer;
    }

    public static ScalableBuffer wrap(byte[] array) {
        ScalableBuffer buffer = new ScalableBuffer();
        buffer.tmpNioBuf = ByteBuffer.wrap(array);
        return buffer;
    }

    public byte[] getArray() {
        tmpNioBuf.flip();
        byte[] array = new byte[tmpNioBuf.remaining()];
        tmpNioBuf.get(array);
        tmpNioBuf.compact();
        return array;
    }

    public ScalableBuffer get(byte[] array) {
        tmpNioBuf.get(array);
        return this;
    }

    public byte get() {
        return tmpNioBuf.get();
    }

    public ByteBuffer put(byte b) {
        checkCapacity(1);
        return tmpNioBuf.put(b);
    }

    public short getShort() {
        return tmpNioBuf.getShort();
    }

    public ByteBuffer putShort(int value) {
        checkCapacity(2);
        return tmpNioBuf.putShort((short) value);
    }

    public int getInt() {
        return tmpNioBuf.getInt();
    }

    public ByteBuffer putInt(int value) {
        checkCapacity(4);
        return tmpNioBuf.putInt(value);
    }

    public long getLong() {
        return tmpNioBuf.getLong();
    }

    public ByteBuffer putLong(long value) {
        checkCapacity(8);
        return tmpNioBuf.putLong(value);
    }

    public ByteBuffer put(byte[] value) {
        checkCapacity(value.length);
        return tmpNioBuf.put(value);
    }

    public void checkCapacity(int minWritableBytes) {
        int remaining = tmpNioBuf.remaining();
        if (remaining < minWritableBytes) {
            ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity(tmpNioBuf.capacity() + minWritableBytes));
            tmpNioBuf.flip();
            newBuffer.put(tmpNioBuf);
            tmpNioBuf = newBuffer;
        }
    }

    private int newCapacity(int minNewCapacity) {
        int newCapacity = 64;
        while (newCapacity < minNewCapacity) {
            newCapacity <<= 1;
        }
        return newCapacity;
    }

    public ByteBuffer getNioBuffer(int minNewCapacity) {
        checkCapacity(minNewCapacity);
        return tmpNioBuf;
    }

    public ByteBuffer getNioBuffer() {
        return tmpNioBuf;
    }

    public ScalableBuffer clear() {
        tmpNioBuf.clear();
        return this;
    }

    public ScalableBuffer flip() {
        tmpNioBuf.flip();
        return this;
    }
}
