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
import com.mpush.api.push.PushContext;
import com.mpush.util.DefaultLogger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 2016/1/25.
 *
 * @author ohun@live.cn (夜色)
 */
public class MPushClientTest {
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
    private static final String allocServer = "http://127.0.0.1:9999/";

    public static void main(String[] args) throws Exception {
        int count = 1;
        String serverHost = "127.0.0.1";
        int sleep = 1000;

        if (args != null && args.length > 0) {
            count = Integer.parseInt(args[0]);
            if (args.length > 1) {
                serverHost = args[1];
            }
            if (args.length > 2) {
                sleep = Integer.parseInt(args[1]);
            }
        }

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        ClientListener listener = new L(scheduledExecutor);
        Client client = null;
        String cacheDir = MPushClientTest.class.getResource("/").getFile();
        for (int i = 0; i < count; i++) {
            client = ClientConfig
                    .build()
                    .setPublicKey(publicKey)
                    //.setAllotServer(allocServer)
                    .setServerHost(serverHost)
                    .setServerPort(3000)
                    .setDeviceId("deviceId-test" + i)
                    .setOsName("Android")
                    .setOsVersion("6.0")
                    .setClientVersion("2.0")
                    .setUserId("user-" + i)
                    .setTags("tag-" + i)
                    .setSessionStorageDir(cacheDir + i)
                    .setLogger(new DefaultLogger())
                    .setLogEnabled(true)
                    .setEnableHttpProxy(true)
                    .setClientListener(listener)
                    .create();
            client.start();
            Thread.sleep(sleep);
        }
    }

    public static class L implements ClientListener {
        private final ScheduledExecutorService scheduledExecutor;
        boolean flag = true;

        public L(ScheduledExecutorService scheduledExecutor) {
            this.scheduledExecutor = scheduledExecutor;
        }

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
            scheduledExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    client.healthCheck();
                }
            }, heartbeat, heartbeat, TimeUnit.MILLISECONDS);

            //client.push(PushContext.build("test"));

        }

        @Override
        public void onReceivePush(Client client, byte[] content, int messageId) {
            if (messageId > 0) client.ack(messageId);
        }

        @Override
        public void onKickUser(String deviceId, String userId) {

        }

        @Override
        public void onBind(boolean success, String userId) {

        }

        @Override
        public void onUnbind(boolean success, String userId) {

        }
    }
}