package com.mpush.client;

import static com.mpush.api.Constants.MAX_HB_TIMEOUT_COUNT;
import static com.mpush.api.Constants.MAX_RESTART_COUNT;
import static com.mpush.api.Constants.MAX_TOTAL_RESTART_COUNT;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

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

import com.mpush.api.Client;
import com.mpush.api.ClientListener;
import com.mpush.api.Constants;
import com.mpush.api.Logger;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.connection.SessionStorage;
import com.mpush.api.http.HttpRequest;
import com.mpush.api.http.HttpResponse;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.handler.HttpProxyHandler;
import com.mpush.message.BindUserMessage;
import com.mpush.message.ChatMessage;
import com.mpush.message.FastConnectMessage;
import com.mpush.message.HandshakeMessage;
import com.mpush.message.HttpRequestMessage;
import com.mpush.security.AesCipher;
import com.mpush.security.CipherBox;
import com.mpush.session.PersistentSession;
import com.mpush.util.DefaultLogger;
import com.mpush.util.IOUtils;
import com.mpush.util.Strings;
import com.mpush.util.thread.EventLock;
import com.mpush.util.thread.ExecutorManager;

/**
 * Created by ohun on 2016/1/17.
 */
public final class MPushClient implements Client {
	public enum State {
		Starting, Started, Shutdown, Restarting, Destroyed
	}

	public static MPushClient I = new MPushClient();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final AtomicReference<State> clientState = new AtomicReference(State.Shutdown);
	private final Executor executor = ExecutorManager.INSTANCE.getStartThread();
	private final EventLock connLock = new EventLock();

	private final MessageDispatcher receiver;
	private final NioConnection connection;
	
	private static final Logger logger = new DefaultLogger(MPushClient.class);

	private SocketChannel channel;
	private int hbTimeoutTimes;
	private int totalRestartCount;
	private volatile int restartCount = 1;
	private volatile boolean autoRestart = true;
	private HttpRequestQueue requestQueue;
	private String[] lastServerAddress;
	private boolean isInit = false;

	public MPushClient() {
		this.receiver = new MessageDispatcher();
		this.connection = new NioConnection(this, receiver);
		
		this.requestQueue = new HttpRequestQueue();
		this.receiver.register(Command.HTTP_PROXY, new HttpProxyHandler(requestQueue));
	}

	/**
	 * 初始化MPushClient，传入所需的所有参数
	 * 
	 * @param allocUrl
	 *            alloc server的URL
	 * @param publicKey
	 *            消息处理所需的公钥
	 * @param deviceId
	 *            当前设备的ID
	 * @param osName
	 *            操作系统名称：Android 或 IOS
	 * @param osVersion
	 *            操作系统版本号
	 * @param clientVersion
	 *            客户端版本号
	 * @param userId
	 *            当前用户ID
	 * @param listener
	 *            客户端MPush连接监听类，接受消息在这里处理
	 */
	public void initClient(String allocUrl, String publicKey, String deviceId, String osName, String osVersion,
			String clientVersion, String userId, String tokenDir, ClientListener listener) {
		ClientConfig.I.setPublicKey(publicKey);
		ClientConfig.I.setAllocServer(allocUrl);
		ClientConfig.I.setOsName(osName);
		ClientConfig.I.setOsVersion(osVersion);
		ClientConfig.I.setClientVersion(clientVersion);
		ClientConfig.I.setDeviceId(deviceId);
		ClientConfig.I.setUserId(userId);
		ClientConfig.I.setSessionStorageDir(tokenDir);
		ClientConfig.I.setClientListener(listener);
		this.isInit = true;
	}

	/**
	 * 检查是否初始化，成功的标志为传入所需的所有参数
	 * 
	 * @return
	 */
	public boolean isInit() {
		return this.isInit;
	}

	private String[] getServerAddress() {
		HttpURLConnection connection;
		try {
			URL url = new URL(ClientConfig.I.getAllocServer());
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
			logger.d("get server address success result=%s", result);
			return result.split(",");
		}
		logger.w("get server address failure return content empty.");
		return null;
	}

	private boolean connect(String host, int port) {
		connLock.lock();
		logger.d("try connect server [%s:%s]", host, port);
		try {
			channel = SocketChannel.open();
			channel.connect(new InetSocketAddress(host, port));
			connection.init(channel);
			restartCount = 1;
			autoRestart = true;
			clientState.set(State.Started);
			connLock.signalAll();
			connLock.unlock();
			logger.d("connect server ok [%s:%s]", host, port);
			return true;
		} catch (Throwable t) {
			if (clientState.get() == State.Starting && !autoRestart) {
				autoRestart = true;// 处理stop之后autoRestart=false的情况
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
					ClientConfig.I.getClientListener().onDisConnected(this);
					logger.d("channel closed !!!");
				}
				this.channel = null;
			}
		} finally {
			connLock.unlock();
		}
	}

	void restart() {
		if (totalRestartCount > MAX_TOTAL_RESTART_COUNT) {// 过载保护
			logger.w("client total restart count over limit, totalRestartCount=%d, currentState=%s, autoRestart=%b",
					totalRestartCount, clientState.get(), autoRestart);
			return;
		}

		State state = clientState.get();
		if (state == State.Starting || state == State.Restarting) {
			logger.w("client is restarting oldState=%s, currentState=%s, autoRestart=%b", state, clientState.get(),
					autoRestart);
			return;
		}

		connLock.lock();
		logger.d("try restart client count=%d, total=%d, t=%s", restartCount, totalRestartCount,
				Thread.currentThread());
		try {
			if (!autoRestart || !clientState.compareAndSet(state, State.Restarting)) {
				logger.w("1 restart failure oldState=%s, currentState=%s, autoRestart=%b", state, clientState.get(),
						autoRestart);
				return;
			}

			restartCount++;// 记录重连次数
			totalRestartCount++;

			if (restartCount > MAX_RESTART_COUNT) {// 超过此值 sleep 10min
				if (!ExecutorManager.isMPThread())
					return;
				if (connLock.await(MINUTES.toMillis(MAX_TOTAL_RESTART_COUNT)))
					return;
				restartCount = 1;
			} else if (restartCount > 2) {// 第二次重连时开始按秒sleep，然后重试
				if (!ExecutorManager.isMPThread())
					return;
				if (connLock.await(SECONDS.toMillis(restartCount)))
					return;
			}

			if (!autoRestart || clientState.get() != State.Restarting) {
				logger.w("2 restart failure oldState=%s, currentState=%s, autoRestart=%b", state, clientState.get(),
						autoRestart);
				return;
			}
		} finally {
			clientState.compareAndSet(State.Restarting, State.Shutdown);
			connLock.unlock();
		}

		logger.d("do restart client count=%d, total=%d, t=%s", restartCount, totalRestartCount, Thread.currentThread());
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
					if (clientState.get() != State.Starting)
						return;
					String[] address = lastServerAddress == null ? getServerAddress() : lastServerAddress;
					if (address != null && address.length > 0) {
						for (String hp : address) {
							String[] hpa = hp.split(":");
							if (hpa.length != 2)
								continue;
							String host = hpa[0];
							int port = Strings.toInt(hpa[1], 0);
							if (clientState.get() != State.Starting)
								return;
							if (connect(host, port)) {
								logger.d("client started !!!");
								ClientConfig.I.getClientListener().onConnected(MPushClient.this);
								lastServerAddress = new String[] { hp };
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
			logger.d("try start client...");
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
		SessionStorage storage = ClientConfig.I.getSessionStorage();
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
		message.deviceId = ClientConfig.I.getDeviceId();
		message.sessionId = session.sessionId;
		message.maxHeartbeat = ClientConfig.I.getMaxHeartbeat();
		message.minHeartbeat = ClientConfig.I.getMinHeartbeat();
		message.sendRaw();
		connection.getSessionContext().changeCipher(session.cipher);
		logger.d(">>> do fast connect, message=%s", message);
	}

	@Override
	public void handshake() {
		SessionContext context = connection.getSessionContext();
		context.changeCipher(CipherBox.INSTANCE.getRsaCipher());
		HandshakeMessage message = new HandshakeMessage(connection);
		message.clientKey = CipherBox.INSTANCE.randomAESKey();
		message.iv = CipherBox.INSTANCE.randomAESIV();
		message.deviceId = ClientConfig.I.getDeviceId();
		message.osName = ClientConfig.I.getOsName();
		message.osVersion = ClientConfig.I.getOsVersion();
		message.clientVersion = ClientConfig.I.getClientVersion();
		message.maxHeartbeat = ClientConfig.I.getMaxHeartbeat();
		message.minHeartbeat = ClientConfig.I.getMinHeartbeat();
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
		if (userId.equals(context.bindUser))
			return;
		context.setBindUser(userId);
		ClientConfig.I.setUserId(userId);
		BindUserMessage.buildBind(connection).setUserId(userId).send();
		logger.d(">>> do bind user, userId=%s", userId);
	}

	@Override
	public void unbindUser() {
		String userId = ClientConfig.I.getUserId();
		if (Strings.isBlank(userId)) {
			logger.w("unbind user is null");
			return;
		}
		ClientConfig.I.setUserId(null);
		connection.getSessionContext().setBindUser(null);
		BindUserMessage.buildUnbind(connection).setUserId(userId).send();
		logger.d(">>> do unbind user, userId=%s", userId);
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
		logger.e(new Throwable("send http proxy before handshake!!"), "send http proxy before handshake!!");
		return null;
	}

	@Override
	public void sendMsg(String content, String userId, MPushCallback callback) {
		logger.d("send message request:%s, %s", content, userId);
		SessionContext context = connection.getSessionContext();
		context.changeCipher(CipherBox.INSTANCE.getRsaCipher());
		ChatMessage message = new ChatMessage(userId,content,connection);
		message.content = content;
		message.destUserId = userId;
		message.send();
//		StringBuilder uri = new StringBuilder("http://192.168.16.49/mpns/push/push");
//		uri.append("?userId=" + userId);
//		uri.append("&content=" + content);
//		HttpRequest reqMsg = HttpRequest.buildGet(uri.toString());
//		reqMsg.setCallback(callback);
//		reqMsg.setTimeout(3000);
//		this.sendHttp(reqMsg);
	}

	EventLock getConnLock() {
		return connLock;
	}
}
