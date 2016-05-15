package com.mpush.client;

import com.mpush.api.Client;
import com.mpush.api.ClientListener;
import com.mpush.api.connection.SessionStorage;
import com.mpush.session.FileSessionStorage;
import com.mpush.util.DefaultLogger;
import com.mpush.api.Constants;
import com.mpush.api.Logger;

/**
 * Created by ohun on 2016/1/17.
 */
public final class ClientConfig {
    private final DefaultClientListener clientListener = new DefaultClientListener();
    public static ClientConfig I = new ClientConfig();
    private String allotServer;
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
    private Logger logger;
    private boolean logEnabled;
    private boolean enableHttpProxy = true;

    public static ClientConfig build() {
        return I = new ClientConfig();
    }

    public Client create() {
        return new MPushClient(this);
    }

    /*package*/ void destroy() {
        clientListener.setListener(null);
        I = new ClientConfig();
    }

    public SessionStorage getSessionStorage() {
        if (sessionStorage == null) {
            sessionStorage = new FileSessionStorage(sessionStorageDir);
        }
        return sessionStorage;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = new DefaultLogger();
        }
        return logger;
    }

    public ClientConfig setLogger(Logger logger) {
        this.logger = logger;
        this.getLogger().enable(logEnabled);
        return this;
    }

    public String getSessionStorageDir() {
        return sessionStorageDir;
    }

    public ClientConfig setSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
        return this;
    }

    public ClientConfig setSessionStorageDir(String sessionStorageDir) {
        this.sessionStorageDir = sessionStorageDir;
        return this;
    }

    public String getAllotServer() {
        return allotServer;
    }

    public ClientConfig setAllotServer(String allotServer) {
        this.allotServer = allotServer;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public ClientConfig setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getOsName() {
        return osName;
    }

    public ClientConfig setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public ClientConfig setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public ClientConfig setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public int getMaxHeartbeat() {
        return maxHeartbeat;
    }

    public ClientConfig setMaxHeartbeat(int maxHeartbeat) {
        this.maxHeartbeat = maxHeartbeat;
        return this;
    }

    public int getMinHeartbeat() {
        return minHeartbeat;
    }

    public ClientConfig setMinHeartbeat(int minHeartbeat) {
        this.minHeartbeat = minHeartbeat;
        return this;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }

    public ClientConfig setAesKeyLength(int aesKeyLength) {
        this.aesKeyLength = aesKeyLength;
        return this;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public ClientConfig setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public int getCompressLimit() {
        return compressLimit;
    }

    public ClientConfig setCompressLimit(int compressLimit) {
        this.compressLimit = compressLimit;
        return this;
    }

    public ClientListener getClientListener() {
        return clientListener;
    }

    public ClientConfig setClientListener(ClientListener clientListener) {
        this.clientListener.setListener(clientListener);
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public ClientConfig setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public ClientConfig setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
        this.logger.enable(logEnabled);
        return this;
    }

    public boolean isEnableHttpProxy() {
        return enableHttpProxy;
    }

    public ClientConfig setEnableHttpProxy(boolean enableHttpProxy) {
        this.enableHttpProxy = enableHttpProxy;
        return this;
    }
}
