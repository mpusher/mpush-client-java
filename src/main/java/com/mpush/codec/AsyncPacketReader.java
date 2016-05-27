package com.mpush.codec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.mpush.api.Logger;
import com.mpush.api.PacketReader;
import com.mpush.api.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.util.ByteBuf;
import com.mpush.util.DefaultLogger;
import com.mpush.util.thread.ExecutorManager;
import com.mpush.util.thread.NamedThreadFactory;

/**
 * Created by ohun on 2016/1/17.
 */
public final class AsyncPacketReader implements PacketReader, Runnable {
    private final NamedThreadFactory threadFactory = new NamedThreadFactory(ExecutorManager.READ_THREAD_NAME);
    private final Connection connection;
    private final PacketReceiver receiver;
    private final ByteBuf buffer;
    private static final Logger logger = new DefaultLogger(AsyncPacketReader.class);

    private Thread thread;

    public AsyncPacketReader(Connection connection, PacketReceiver receiver) {
        this.connection = connection;
        this.receiver = receiver;
        this.buffer = ByteBuf.allocateDirect(Short.MAX_VALUE);//默认读buffer大小为32k
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
            logger.d("decode one packet=%s", packet);
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
        }
        return readCount > 0;
    }
}
