package com.mpush.client;

import com.mpush.api.*;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.protocol.Packet;
import com.mpush.codec.AsyncPacketReader;
import com.mpush.codec.AsyncPacketWriter;
import com.mpush.util.IOUtils;
import com.mpush.util.Strings;
import com.mpush.util.thread.EventLock;
import com.mpush.util.thread.ExecutorManager;

import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import static com.mpush.api.Constants.MAX_RESTART_COUNT;
import static com.mpush.api.Constants.MAX_TOTAL_RESTART_COUNT;
import static com.mpush.client.NioConnection.State.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by ohun on 2016/1/21.
 */
public final class NioConnection implements Connection {
    private final AtomicReference<State> state = new AtomicReference(disconnected);
    private final ThreadPoolExecutor executor = ExecutorManager.INSTANCE.getStartThread();
    private final EventLock connLock = new EventLock();
    private volatile int restartCount = 1;
    private final ClientConfig config;
    private final Logger logger;
    private final ClientListener listener;
    private final MPushClient client;
    private final PacketWriter writer;
    private final PacketReader reader;
    private final AllotClient allotClient;
    private SocketChannel channel;
    private SessionContext context;
    private long lastReadTime;
    private long lastWriteTime;
    private int hbTimeoutTimes;
    private int totalRestartCount;
    private Future<?> currentTask;
    private ConnectThread connectThread;

    public enum State {connecting, connected, disconnecting, disconnected}

    public NioConnection(MPushClient client, PacketReceiver receiver) {
        this.client = client;
        this.config = ClientConfig.I;
        this.logger = config.getLogger();
        this.listener = config.getClientListener();
        this.allotClient = new AllotClient();
        this.reader = new AsyncPacketReader(this, receiver);
        this.writer = new AsyncPacketWriter(this, client.getConnLock());
    }

    private void init(SocketChannel channel) {
        this.restartCount = 1;
        this.channel = channel;
        this.context = new SessionContext();
        this.state.set(connected);
        this.reader.startRead();
    }


    @Override
    public void connect() {
        if (state.compareAndSet(disconnected, connecting)) {
            if ((connectThread == null) || !connectThread.isAlive()) {
                connectThread = new ConnectThread(connLock);
            }

            connectThread.addConnectTask(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return doReconnect();
                }
            });
        }
    }

    @Override
    public void reconnect() {
        close();
        connect();
    }

    @Override
    public void close() {
        if (state.compareAndSet(connected, disconnecting)) {
            reader.stopRead();
            if(connectThread!)
            doClose();
        }
    }

    private void doClose() {
        connLock.lock();

        try {
            Channel channel = this.channel;

            if (channel != null) {
                if (channel.isOpen()) {
                    IOUtils.close(channel);
                    listener.onDisConnected(client);
                    logger.w("channel closed !!!");
                }

                this.channel = null;
            }
        } finally {
            state.set(disconnected);
            connLock.unlock();
        }
    }


    private boolean doConnect() {
        List<String> address = allotClient.getServerAddress();

        if ((address != null) && (address.size() > 0)) {
            Iterator<String> it = address.iterator();

            while (it.hasNext()) {
                String[] host_port = it.next().split(":");

                if (host_port.length == 2) {
                    String host = host_port[0];
                    int port = Strings.toInt(host_port[1], 0);

                    if (doConnect(host, port)) {
                        logger.w("client started !!!");
                        listener.onConnected(client);

                        return true;
                    }
                }

                it.remove();
            }
        }

        return false;
    }

    private boolean doConnect(String host, int port) {
        connLock.lock();
        logger.w("try connect server [%s:%s]", host, port);

        try {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(host, port));
            init(channel);
            connLock.signalAll();
            connLock.unlock();
            logger.w("connect server ok [%s:%s]", host, port);
            return true;
        } catch (Throwable t) {
            IOUtils.close(channel);
            connLock.unlock();
            logger.e(t, "connect server ex, [%s:%s]", host, port);
        }

        return false;
    }

    private boolean doReconnect() {
        if (totalRestartCount > MAX_TOTAL_RESTART_COUNT) {    // 过载保护
            logger.w("client total doReconnect count over limit, totalRestartCount=%d, currentState=%s",
                    totalRestartCount,
                    state.get());

            return true;
        }

        restartCount++;    // 记录重连次数
        totalRestartCount++;
        connLock.lock();
        logger.d("try doReconnect client count=%d, total=%d, t=%s",
                restartCount,
                totalRestartCount,
                Thread.currentThread());

        try {
            if (restartCount > MAX_RESTART_COUNT) {    // 超过此值 sleep 10min
                if (connLock.await(MINUTES.toMillis(10))) {
                    return false;
                }

                restartCount = 1;
            } else if (restartCount > 2) {             // 第二次重连时开始按秒sleep，然后重试
                if (connLock.await(SECONDS.toMillis(restartCount))) {
                    return false;
                }
            }
        } finally {
            connLock.unlock();
        }

        if (Thread.currentThread().isInterrupted() || state.get() != connecting) {
            logger.w("2 doReconnect failure state=%s", state.get());

            return true;
        }

        logger.w("do doReconnect client count=%d, total=%d, t=%s",
                restartCount,
                totalRestartCount,
                Thread.currentThread());

        return doConnect();
    }

    @Override
    public void send(Packet packet) {
        writer.write(packet);
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
    public boolean isConnected() {
        return state.get() == connected;
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
    public boolean isReadTimeout() {
        return System.currentTimeMillis() - lastReadTime > context.heartbeat + 1000;
    }

    @Override
    public SessionContext getSessionContext() {
        return context;
    }

    @Override
    public boolean isWriteTimeout() {
        return System.currentTimeMillis() - lastWriteTime > context.heartbeat - 1000;
    }

    @Override
    public String toString() {
        return "NioConnection{" + ", lastReadTime=" + lastReadTime + ", lastWriteTime=" + lastWriteTime + ", context="
                + context + '}';
    }
}
