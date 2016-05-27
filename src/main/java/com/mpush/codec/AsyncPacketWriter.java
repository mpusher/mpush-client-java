package com.mpush.codec;

import static com.mpush.api.Constants.DEFAULT_WRITE_TIMEOUT;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import com.mpush.api.Logger;
import com.mpush.api.PacketWriter;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.util.ByteBuf;
import com.mpush.util.DefaultLogger;
import com.mpush.util.thread.EventLock;
import com.mpush.util.thread.ExecutorManager;

/**
 * Created by ohun on 2016/1/17.
 */
public final class AsyncPacketWriter implements PacketWriter {
    private final Executor executor = ExecutorManager.INSTANCE.getWriteThread();
    private final Connection connection;
    private final EventLock connLock;
    private final ByteBuf buffer;
    private static final Logger logger = new DefaultLogger(AsyncPacketWriter.class);

    public AsyncPacketWriter(Connection connection, EventLock connLock) {
        this.connection = connection;
        this.connLock = connLock;
        this.buffer = ByteBuf.allocateDirect(1024);//默认写buffer为1k
    }

    public void write(Packet packet) {
        executor.execute(new WriteTask(packet));
    }

    private class WriteTask implements Runnable {
        private final long sendTime = System.currentTimeMillis();
        private final Packet packet;

        private WriteTask(Packet packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            buffer.clear();
            PacketEncoder.encode(packet, buffer);
            buffer.flip();
            ByteBuffer out = buffer.nioBuffer();
            while (out.hasRemaining()) {
                if (connection.isConnected()) {
                    try {
                        connection.getChannel().write(out);
                        connection.setLastWriteTime();
                    } catch (IOException e) {
                        logger.e(e, "write packet ex, do reconnect, packet=%s", packet);
                        if (isTimeout()) {
                            logger.w("ignored timeout packet=%s, sendTime=%d", packet, sendTime);
                            return;
                        }
                        connection.reconnect();
                    }
                } else if (isTimeout()) {
                    logger.w("ignored timeout packet=%s, sendTime=%d", packet, sendTime);
                    return;
                } else {
                    connLock.await(DEFAULT_WRITE_TIMEOUT);
                }
            }
            logger.d("write packet end, packet=%s, costTime=%d", packet.cmd, (System.currentTimeMillis() - sendTime));
        }

        public boolean isTimeout() {
            return System.currentTimeMillis() - sendTime > DEFAULT_WRITE_TIMEOUT;
        }
    }
}
