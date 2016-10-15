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


import com.mpush.api.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.util.thread.ExecutorManager;
import com.mpush.client.ClientConfig;
import com.mpush.api.Logger;
import com.mpush.api.PacketReader;
import com.mpush.util.thread.NamedThreadFactory;
import com.mpush.util.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by ohun on 2016/1/17.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AsyncPacketReader implements PacketReader, Runnable {
    private final NamedThreadFactory threadFactory = new NamedThreadFactory(ExecutorManager.READ_THREAD_NAME);
    private final Connection connection;
    private final PacketReceiver receiver;
    private final ByteBuf buffer;
    private final Logger logger;

    private Thread thread;

    public AsyncPacketReader(Connection connection, PacketReceiver receiver) {
        this.connection = connection;
        this.receiver = receiver;
        this.buffer = ByteBuf.allocateDirect(Short.MAX_VALUE);//默认读buffer大小为32k
        this.logger = ClientConfig.I.getLogger();
    }

    @Override
    public synchronized void startRead() {
        this.thread = threadFactory.newThread(this);
        this.thread.start();
    }

    @Override
    public synchronized void stopRead() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void run() {
        try {
            this.buffer.clear();
            while (connection.isConnected()) {
                ByteBuffer in = buffer.checkCapacity(1024).nioBuffer();//如果剩余空间不够每次增加1k
                if (!read(connection.getChannel(), in)) break;
                in.flip();
                decodePacket(in);
                in.compact();
            }
        } finally {
            logger.w("read an error, do reconnect!!!");
            connection.reconnect();
        }
    }

    private void decodePacket(ByteBuffer in) {
        Packet packet;
        while ((packet = PacketDecoder.decode(in)) != null) {
            //  logger.d("decode one packet=%s", packet);
            receiver.onReceive(packet, connection);
        }
    }

    private boolean read(SocketChannel channel, ByteBuffer in) {
        int readCount;
        try {
            readCount = channel.read(in);
            connection.setLastReadTime();
        } catch (IOException e) {
            logger.e(e, "read packet ex, do reconnect");
            readCount = -1;
            sleep4Reconnect();
        }
        return readCount > 0;
    }

    private void sleep4Reconnect() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }
}
