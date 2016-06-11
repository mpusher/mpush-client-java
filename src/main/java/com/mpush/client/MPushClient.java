package com.mpush.client;

import com.mpush.api.Client;
import com.mpush.api.ClientListener;
import com.mpush.api.Logger;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.connection.SessionStorage;
import com.mpush.api.http.HttpRequest;
import com.mpush.api.http.HttpResponse;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.handler.HttpProxyHandler;
import com.mpush.message.BindUserMessage;
import com.mpush.message.FastConnectMessage;
import com.mpush.message.HandshakeMessage;
import com.mpush.message.HttpRequestMessage;
import com.mpush.security.AesCipher;
import com.mpush.security.CipherBox;
import com.mpush.session.PersistentSession;
import com.mpush.util.IOUtils;
import com.mpush.util.Strings;
import com.mpush.util.thread.EventLock;
import com.mpush.util.thread.ExecutorManager;

import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static com.mpush.api.Constants.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by ohun on 2016/1/17.
 */
public final class MPushClient implements Client {
    public enum State {Starting, Started, Shutdown, Restarting, Destroyed}


    private final AtomicReference<State> clientState = new AtomicReference(State.Shutdown);


    private final MessageDispatcher receiver;
    private final NioConnection connection;
    private final ClientConfig config;
    private final Logger logger;
    private final ClientListener listener;

    private HttpRequestQueue requestQueue;

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





    @Override
    public void start() {

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
            totalRestartCount = 0;
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

        if (connection.isReadTimeout()) {
            hbTimeoutTimes++;
            logger.w("heartbeat timeout times=%s", hbTimeoutTimes);
        } else {
            hbTimeoutTimes = 0;
        }

        if (hbTimeoutTimes >= MAX_HB_TIMEOUT_COUNT) {
            logger.w("heartbeat timeout times=%d over limit=%d, client restart", hbTimeoutTimes, MAX_HB_TIMEOUT_COUNT);
            hbTimeoutTimes = 0;
            restart();
            return false;
        }

        if (connection.isWriteTimeout()) {
            logger.d(">>> send heartbeat ping...");
            connection.send(Packet.HB_PACKET);
        }

        return true;
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
        logger.w(">>> do fast connect, message=%s", message);
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
        logger.w(">>> do handshake, message=%s", message);
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
        logger.w(">>> do bind user, userId=%s", userId);
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
        logger.w(">>> do unbind user, userId=%s", userId);
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
            logger.d(">>> send http proxy, request=%s", request);
            return requestQueue.add(message.getSessionId(), request);
        }
        return null;
    }

    EventLock getConnLock() {
        return connLock;
    }
}
