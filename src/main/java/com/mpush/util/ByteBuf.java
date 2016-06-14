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

package com.mpush.util;


import java.nio.*;

/**
 * Created by ohun on 2016/1/21.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ByteBuf {
    private ByteBuffer tmpNioBuf;

    public static ByteBuf allocate(int capacity) {
        ByteBuf buffer = new ByteBuf();
        buffer.tmpNioBuf = ByteBuffer.allocate(capacity);
        return buffer;
    }

    public static ByteBuf allocateDirect(int capacity) {
        ByteBuf buffer = new ByteBuf();
        buffer.tmpNioBuf = ByteBuffer.allocateDirect(capacity);
        return buffer;
    }

    public static ByteBuf wrap(byte[] array) {
        ByteBuf buffer = new ByteBuf();
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

    public ByteBuf get(byte[] array) {
        tmpNioBuf.get(array);
        return this;
    }

    public byte get() {
        return tmpNioBuf.get();
    }

    public ByteBuf put(byte b) {
        checkCapacity(1);
        tmpNioBuf.put(b);
        return this;
    }

    public short getShort() {
        return tmpNioBuf.getShort();
    }

    public ByteBuf putShort(int value) {
        checkCapacity(2);
        tmpNioBuf.putShort((short) value);
        return this;
    }

    public int getInt() {
        return tmpNioBuf.getInt();
    }

    public ByteBuf putInt(int value) {
        checkCapacity(4);
        tmpNioBuf.putInt(value);
        return this;
    }

    public long getLong() {
        return tmpNioBuf.getLong();
    }

    public ByteBuf putLong(long value) {
        checkCapacity(8);
        tmpNioBuf.putLong(value);
        return this;
    }

    public ByteBuf put(byte[] value) {
        checkCapacity(value.length);
        tmpNioBuf.put(value);
        return this;
    }

    public ByteBuf checkCapacity(int minWritableBytes) {
        int remaining = tmpNioBuf.remaining();
        if (remaining < minWritableBytes) {
            int newCapacity = newCapacity(tmpNioBuf.capacity() + minWritableBytes);
            ByteBuffer newBuffer = tmpNioBuf.isDirect() ? ByteBuffer.allocateDirect(newCapacity) : ByteBuffer.allocate(newCapacity);
            tmpNioBuf.flip();
            newBuffer.put(tmpNioBuf);
            tmpNioBuf = newBuffer;
        }
        return this;
    }

    private int newCapacity(int minNewCapacity) {
        int newCapacity = 64;
        while (newCapacity < minNewCapacity) {
            newCapacity <<= 1;
        }
        return newCapacity;
    }

    public ByteBuffer nioBuffer() {
        return tmpNioBuf;
    }

    public ByteBuf clear() {
        tmpNioBuf.clear();
        return this;
    }

    public ByteBuf flip() {
        tmpNioBuf.flip();
        return this;
    }
}
