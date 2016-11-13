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
 *
 * @author ohun@live.cn (夜色)
 */
public final class ClientConfig {
    private final DefaultClientListener clientListener = new DefaultClientListener();
    public static ClientConfig I = new ClientConfig();
    private String allotServer;
    private String serverHost;
    private int serverPort;
    private String publicKey;
    private String deviceId;
    private String osName = Constants.DEF_OS_NAME;
    private String osVersion;
    private String clientVersion;
    private String userId;
    private String tags;
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

    public String getServerHost() {
        return serverHost;
    }

    public ClientConfig setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public int getServerPort() {
        return serverPort;
    }

    public ClientConfig setServerPort(int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public ClientConfig setTags(String tags) {
        this.tags = tags;
        return this;
    }
}
