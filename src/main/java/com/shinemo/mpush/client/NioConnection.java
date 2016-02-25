package com.shinemo.mpush.client;

import com.shinemo.mpush.api.*;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.codec.AsyncPacketReader;
import com.shinemo.mpush.codec.AsyncPacketWriter;

import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ohun on 2016/1/21.
 */
public final class NioConnection implements Connection {
    public enum State {Created, Connected, DisConnected}

    private final AtomicReference<State> connectionState = new AtomicReference(State.Created);

    private final MPushClient client;
    private final PacketWriter writer;
    private final PacketReader reader;

    private SocketChannel channel;
    private SessionContext context;
    private long lastReadTime;
    private long lastWriteTime;

    public NioConnection(MPushClient client, PacketReceiver receiver) {
        this.client = client;
        this.reader = new AsyncPacketReader(this, receiver);
        this.writer = new AsyncPacketWriter(this, client.getConnLock());
    }

    synchronized void init(SocketChannel channel) {
        this.channel = channel;
        this.context = new SessionContext();
        this.connectionState.set(State.Connected);
        this.reader.startRead();
    }

    @Override
    public SessionContext getSessionContext() {
        return context;
    }

    @Override
    public void send(Packet packet) {
        writer.write(packet);
    }

    @Override
    public void close() {
        connectionState.set(State.DisConnected);
        reader.stopRead();
        client.closeChannel();
    }

    @Override
    public boolean isConnected() {
        return connectionState.get() == State.Connected;
    }

    @Override
    public void reconnect() {
        connectionState.set(State.DisConnected);
        client.restart();
    }

    @Override
    public boolean isReadTimeout() {
        return System.currentTimeMillis() - lastReadTime > context.heartbeat + 1000;
    }

    @Override
    public boolean isWriteTimeout() {
        return System.currentTimeMillis() - lastWriteTime > context.heartbeat - 1000;
    }

    @Override
    public void setLastReadTime() {
        lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void setLastWriteTime() {
        lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public String toString() {
        return "NioConnection{" +
                ", lastReadTime=" + lastReadTime +
                ", lastWriteTime=" + lastWriteTime +
                ", context=" + context +
                '}';
    }
}
