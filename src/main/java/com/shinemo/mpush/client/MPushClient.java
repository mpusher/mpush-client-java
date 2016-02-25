package com.shinemo.mpush.client;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.ClientListener;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.connection.SessionStorage;
import com.shinemo.mpush.api.http.HttpRequest;
import com.shinemo.mpush.api.http.HttpResponse;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.handler.HttpProxyHandler;
import com.shinemo.mpush.message.BindUserMessage;
import com.shinemo.mpush.message.FastConnectMessage;
import com.shinemo.mpush.message.HandshakeMessage;
import com.shinemo.mpush.message.HttpRequestMessage;
import com.shinemo.mpush.security.AesCipher;
import com.shinemo.mpush.security.CipherBox;
import com.shinemo.mpush.session.PersistentSession;
import com.shinemo.mpush.util.IOUtils;
import com.shinemo.mpush.util.Strings;
import com.shinemo.mpush.util.thread.EventLock;
import com.shinemo.mpush.util.thread.ExecutorManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static com.shinemo.mpush.api.Constants.MAX_HB_TIMEOUT_COUNT;
import static com.shinemo.mpush.api.Constants.MAX_TOTAL_RESTART_COUNT;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by ohun on 2016/1/17.
 */
public final class MPushClient implements Client {
    public enum State {Starting, Started, Shutdown, Restarting, Destroyed}


    private final AtomicReference<State> clientState = new AtomicReference(State.Shutdown);
    private final Executor executor = ExecutorManager.INSTANCE.getStartThread();
    private final EventLock connLock = new EventLock();

    private final MessageDispatcher receiver;
    private final NioConnection connection;
    private final ClientConfig config;
    private final Logger logger;
    private final ClientListener listener;

    private SocketChannel channel;
    private int hbTimeoutTimes;
    private volatile int restartCount = 1;
    private volatile boolean autoRestart = true;
    private HttpRequestQueue requestQueue;
    private String[] lastServerAddress;

    /*package*/ MPushClient(ClientConfig config) {
        this.config = config;
        this.logger = config.getLogger();
        this.listener = config.getClientListener();
        this.receiver = new MessageDispatcher();
        this.connection = new NioConnection(this, receiver);
        if (config.isEnableHttpProxy()) {
            this.requestQueue = new HttpRequestQueue();
            this.receiver.register(Command.HTTP_PROXY, new HttpProxyHandler(requestQueue));
        }
    }

    private String[] getServerAddress() {
        HttpURLConnection connection;
        try {
            URL url = new URL(config.getAllotServer());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                logger.w("get server address failure statusCode=%d", statusCode);
                connection.disconnect();
                return null;
            }
        } catch (IOException e) {
            logger.e(e, "get server address ex, when connect server.");
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(128);
        byte[] buffer = new byte[128];
        InputStream in = null;
        try {
            in = connection.getInputStream();
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        } catch (IOException ioe) {
            logger.e(ioe, "get server address ex, when read result.");
            return null;
        } finally {
            IOUtils.close(in);
            connection.disconnect();
        }
        byte[] content = out.toByteArray();
        if (content.length > 0) {
            String result = new String(content, Constants.UTF_8);
            logger.w("get server address success result=%s", result);
            return result.split(",");
        }
        logger.w("get server address failure return content empty.");
        return null;
    }

    private boolean connect(String host, int port) {
        connLock.lock();
        logger.w("try connect server [%s:%s]", host, port);
        try {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(host, port));
            connection.init(channel);
            restartCount = 1;
            autoRestart = true;
            clientState.set(State.Started);
            connLock.signalAll();
            connLock.unlock();
            logger.w("connect server ok [%s:%s]", host, port);
            return true;
        } catch (Throwable t) {
            if (clientState.get() == State.Starting && !autoRestart) {
                autoRestart = true;//处理stop之后autoRestart=false的情况
            }
            IOUtils.close(channel);
            connLock.unlock();
            logger.e(t, "connect server ex, [%s:%s]", host, port);
        }
        return false;
    }

    void closeChannel() {
        connLock.lock();
        try {
            Channel channel = this.channel;
            if (channel != null) {
                clientState.set(State.Shutdown);
                if (channel.isOpen()) {
                    IOUtils.close(channel);
                    listener.onDisConnected(this);
                    logger.w("channel closed !!!");
                }
                this.channel = null;
            }
        } finally {
            connLock.unlock();
        }
    }

    void restart() {
        State state = clientState.get();
        if (state == State.Starting || state == State.Restarting) {
            logger.w("client is restarting oldState=%s, currentState=%s, autoRestart=%b"
                    , state, clientState.get(), autoRestart);
            return;
        }

        connLock.lock();
        logger.d("try restart client count=%d, t=%s", restartCount, Thread.currentThread());
        try {
            if (!autoRestart || !clientState.compareAndSet(state, State.Restarting)) {
                logger.w("1 restart failure oldState=%s, currentState=%s, autoRestart=%b"
                        , state, clientState.get(), autoRestart);
                return;
            }

            restartCount++;//记录重连次数

            if (restartCount > MAX_TOTAL_RESTART_COUNT) {//超过此值sleep 10min
                if (!ExecutorManager.isMPThread()) return;
                if (connLock.await(MINUTES.toMillis(MAX_TOTAL_RESTART_COUNT))) return;
                restartCount = 1;
            } else if (restartCount > 2) {//第二次重连时开始按秒sleep，然后重试
                if (!ExecutorManager.isMPThread()) return;
                if (connLock.await(SECONDS.toMillis(restartCount))) return;
            }

            if (!autoRestart || clientState.get() != State.Restarting) {
                logger.w("2 restart failure oldState=%s, currentState=%s, autoRestart=%b"
                        , state, clientState.get(), autoRestart);
                return;
            }
        } finally {
            clientState.compareAndSet(State.Restarting, State.Shutdown);
            connLock.unlock();
        }
        logger.w("do restart client count=%d, t=%s", restartCount, Thread.currentThread());
        closeChannel();
        start();
    }

    @Override
    public void start() {
        connLock.lock();
        if (clientState.compareAndSet(State.Shutdown, State.Starting)) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (clientState.get() != State.Starting) return;
                    String[] address = lastServerAddress == null ? getServerAddress() : lastServerAddress;
                    if (address != null && address.length > 0) {
                        for (String hp : address) {
                            String[] hpa = hp.split(":");
                            if (hpa.length != 2) continue;
                            String host = hpa[0];
                            int port = Strings.toInt(hpa[1], 0);
                            if (clientState.get() != State.Starting) return;
                            if (connect(host, port)) {
                                logger.w("client started !!!");
                                listener.onConnected(MPushClient.this);
                                lastServerAddress = new String[]{hp};
                                return;
                            } else {
                                lastServerAddress = null;
                            }
                        }
                    }
                    if (clientState.compareAndSet(State.Starting, State.Shutdown)) {
                        restart();
                    }
                }
            });
            logger.w("try start client...");
        }
        connLock.unlock();
    }

    @Override
    public void stop() {
        connLock.lock();
        logger.w("client shutdown !!!, state=%s", clientState.get());
        autoRestart = false;
        if (clientState.get() != State.Shutdown) {
            clientState.set(State.Shutdown);
            connection.close();
            restartCount = 1;
            connLock.signalAll();
        }
        connLock.unlock();
    }

    @Override
    public void destroy() {
        if (clientState.getAndSet(State.Destroyed) != State.Destroyed) {
            this.stop();
            logger.w("client destroy !!!");
            ExecutorManager.INSTANCE.shutdown();
            ClientConfig.I.destroy();
        }
    }

    @Override
    public boolean isRunning() {
        return clientState.get() == State.Started;
    }

    @Override
    public boolean healthCheck() {
        if (hbTimeoutTimes >= MAX_HB_TIMEOUT_COUNT) {
            logger.w("heartbeat timeout times=%d over limit=%d, client restart", hbTimeoutTimes, MAX_HB_TIMEOUT_COUNT);
            hbTimeoutTimes = 0;
            restart();
            return false;
        }

        if (connection.isWriteTimeout()) {
            logger.d("send heartbeat ping...");
            connection.send(Packet.HB_PACKET);
        }

        if (connection.isReadTimeout()) {
            hbTimeoutTimes++;
            logger.w("heartbeat timeout times=%s", hbTimeoutTimes);
            return false;
        } else {
            hbTimeoutTimes = 0;
            return true;
        }
    }

    @Override
    public void fastConnect() {
        SessionStorage storage = config.getSessionStorage();
        if (storage == null) {
            handshake();
            return;
        }

        String ss = storage.getSession();
        if (Strings.isBlank(ss)) {
            handshake();
            return;
        }

        PersistentSession session = PersistentSession.decode(ss);
        if (session == null || session.isExpired()) {
            storage.clearSession();
            logger.w("fast connect failure session expired, session=%s", session);
            handshake();
            return;
        }

        FastConnectMessage message = new FastConnectMessage(connection);
        message.deviceId = config.getDeviceId();
        message.sessionId = session.sessionId;
        message.maxHeartbeat = config.getMaxHeartbeat();
        message.minHeartbeat = config.getMinHeartbeat();
        message.sendRaw();
        connection.getSessionContext().changeCipher(session.cipher);
        logger.w("do fast connect, message=%s", message);
    }

    @Override
    public void handshake() {
        SessionContext context = connection.getSessionContext();
        context.changeCipher(CipherBox.INSTANCE.getRsaCipher());
        HandshakeMessage message = new HandshakeMessage(connection);
        message.clientKey = CipherBox.INSTANCE.randomAESKey();
        message.iv = CipherBox.INSTANCE.randomAESIV();
        message.deviceId = config.getDeviceId();
        message.osName = config.getOsName();
        message.osVersion = config.getOsVersion();
        message.clientVersion = config.getClientVersion();
        message.maxHeartbeat = config.getMaxHeartbeat();
        message.minHeartbeat = config.getMinHeartbeat();
        message.send();
        context.changeCipher(new AesCipher(message.clientKey, message.iv));
        logger.w("do handshake, message=%s", message);
    }

    @Override
    public void bindUser(String userId) {
        if (Strings.isBlank(userId)) {
            logger.w("bind user is null");
            return;
        }
        SessionContext context = connection.getSessionContext();
        if (userId.equals(context.bindUser)) return;
        context.setBindUser(userId);
        config.setUserId(userId);
        BindUserMessage
                .buildBind(connection)
                .setUserId(userId)
                .send();
        logger.w("do bind user, userId=%s", userId);
    }

    @Override
    public void unbindUser() {
        String userId = config.getUserId();
        if (Strings.isBlank(userId)) {
            logger.w("unbind user is null");
            return;
        }
        config.setUserId(null);
        connection.getSessionContext().setBindUser(null);
        BindUserMessage
                .buildUnbind(connection)
                .setUserId(userId)
                .send();
        logger.w("do unbind user, userId=%s", userId);
    }

    @Override
    public Future<HttpResponse> sendHttp(HttpRequest request) {
        if (connection.getSessionContext().handshakeOk()) {
            HttpRequestMessage message = new HttpRequestMessage(connection);
            message.method = request.method;
            message.uri = request.uri;
            message.headers = request.headers;
            message.body = request.body;
            message.send();
            return requestQueue.add(message.getSessionId(), request);
        }
        return null;
    }

    EventLock getConnLock() {
        return connLock;
    }
}
