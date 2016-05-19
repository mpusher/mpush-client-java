package com.mpush.client;

import com.mpush.api.ClientListener;
import com.mpush.api.Constants;
import com.mpush.api.connection.SessionStorage;
import com.mpush.session.FileSessionStorage;

/**
 * 客户端连接MPush所需的所有的配置项
 * @author maksimwei
 *
 */
public final class ClientConfig {
    private final DefaultClientListener clientListener = new DefaultClientListener();
    public static ClientConfig I = new ClientConfig();
    private String allocServer;
    private String publicKey;
    private String deviceId;
    private String osName = Constants.DEF_OS_NAME;
    private String osVersion;
    private String clientVersion;
    private String userId;
    private int maxHeartbeat = Constants.DEF_HEARTBEAT;
    private int minHeartbeat = Constants.DEF_HEARTBEAT;
    private int aesKeyLength = 16;
    private int compressLimit = Constants.DEF_COMPRESS_LIMIT;
    private SessionStorage sessionStorage;
    private String sessionStorageDir;
//    private static final Logger logger = new DefaultLogger(ClientConfig.class);
//    private boolean logEnabled;
//    private boolean enableHttpProxy = true;

//    /**
//     * 返回全局唯一的ClientConfig实例
//     * @return
//     */
//    public static ClientConfig build() {
//        return I;
//    }

//    /**
//     * 根据当前的配置项，new MPushClient
//     * @return
//     */
//    public Client createMPushClient() {
//        return new MPushClient(this);
//    }

    void destroy() {
        clientListener.setListener(null);
        I = new ClientConfig();
    }

    public SessionStorage getSessionStorage() {
        if (sessionStorage == null) {
            sessionStorage = new FileSessionStorage(sessionStorageDir);
        }
        return sessionStorage;
    }

    public String getSessionStorageDir() {
        return sessionStorageDir;
    }

    public void setSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
//        return this;
    }

    public void setSessionStorageDir(String sessionStorageDir) {
        this.sessionStorageDir = sessionStorageDir;
//        return this;
    }

    public String getAllocServer() {
        return allocServer;
    }

    public void setAllocServer(String allocServer) {
        this.allocServer = allocServer;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
//        return this;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
//        return this;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
//        return this;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
//        return this;
    }

    public int getMaxHeartbeat() {
        return maxHeartbeat;
    }

    public void setMaxHeartbeat(int maxHeartbeat) {
        this.maxHeartbeat = maxHeartbeat;
//        return this;
    }

    public int getMinHeartbeat() {
        return minHeartbeat;
    }

    public void setMinHeartbeat(int minHeartbeat) {
        this.minHeartbeat = minHeartbeat;
//        return this;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }

    public void setAesKeyLength(int aesKeyLength) {
        this.aesKeyLength = aesKeyLength;
//        return this;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
//        return this;
    }

    public int getCompressLimit() {
        return compressLimit;
    }

    public void setCompressLimit(int compressLimit) {
        this.compressLimit = compressLimit;
//        return this;
    }

    public ClientListener getClientListener() {
        return clientListener;
    }

    public void setClientListener(ClientListener clientListener) {
        this.clientListener.setListener(clientListener);
//        return this;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

//    public boolean isLogEnabled() {
//        return logEnabled;
//    }
//
//    public ClientConfig setLogEnabled(boolean logEnabled) {
//        this.logEnabled = logEnabled;
//        this.logger.enable(logEnabled);
//        return this;
//    }
//
//    public boolean isEnableHttpProxy() {
//        return enableHttpProxy;
//    }
//
//    public ClientConfig setEnableHttpProxy(boolean enableHttpProxy) {
//        this.enableHttpProxy = enableHttpProxy;
//        return this;
//    }
}
