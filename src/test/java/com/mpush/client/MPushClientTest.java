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
import com.mpush.api.Constants;
import com.mpush.api.http.HttpCallback;
import com.mpush.api.http.HttpMethod;
import com.mpush.api.http.HttpRequest;
import com.mpush.api.http.HttpResponse;
import com.mpush.util.DefaultLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ohun on 2016/1/25.
 *
 * @author ohun@live.cn (夜色)
 */
public class MPushClientTest {
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
    private static final String allocServer = "http://127.0.0.1:9999/";

    public static void main(String[] args) throws Exception {
        Client client = ClientConfig
                .build()
                .setPublicKey(publicKey)
                //.setAllotServer(allocServer)
                .setServerHost("111.1.57.148")
                .setServerPort(20882)
                .setDeviceId("1111111111")
                .setOsName("Android")
                .setOsVersion("6.0")
                .setClientVersion("2.0")
                .setUserId("doctor43test")
                .setMaxHeartbeat(10000)
                .setMinHeartbeat(10000)
                .setSessionStorageDir(MPushClientTest.class.getResource("/").getFile())
                .setLogger(new DefaultLogger())
                .setLogEnabled(true)
                .setEnableHttpProxy(true)
                .setClientListener(new L())
                .create();
        client.start();

        LockSupport.park();
    }


    public static class L implements ClientListener {
        Thread thread;
        boolean flag = true;

        @Override
        public void onConnected(Client client) {
            flag = true;
        }

        @Override
        public void onDisConnected(Client client) {
            flag = false;
        }

        @Override
        public void onHandshakeOk(final Client client, final int heartbeat) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (flag && client.isRunning()) {
                        try {
                            Thread.sleep(heartbeat);
                        } catch (InterruptedException e) {
                            break;
                        }
                        client.healthCheck();
                        client.stop();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            break;
                        }
                        client.start();
                    }
                }
            });
            thread.start();
        }

        @Override
        public void onReceivePush(Client client, String content) {

        }

        @Override
        public void onKickUser(String deviceId, String userId) {

        }

    }
}