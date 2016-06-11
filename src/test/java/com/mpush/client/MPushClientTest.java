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
 */
public class MPushClientTest {
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
    private static final String allocServer = "http://127.0.0.1:9999/";

    public static void main(String[] args) throws Exception {
        Client client = ClientConfig
                .build()
                .setPublicKey(publicKey)
                .setAllotServer(allocServer)
                .setDeviceId("1111111111")
                .setOsName("Android")
                .setOsVersion("6.0")
                .setClientVersion("2.0")
                .setUserId("doctor43test")
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
                    }
                }
            });
            thread.start();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            HttpRequest request = new HttpRequest(HttpMethod.POST, "http://test.supplier.bicaijia.com/supplier/sendMessage.do");
            request.headers = headers;
            request.body = "srcUserAccountId=5992&destUserAccountId=1796&prevMessageId=0&messageType=0&content=irirjj&refMessageType=0".getBytes(Constants.UTF_8);
            request.timeout = 10000;
            request.callback = new HttpCallback() {
                @Override
                public void onResponse(HttpResponse response) {
                    if (response.statusCode == 200 && response.body != null) {
                        System.out.println(new String(response.body, Constants.UTF_8));
                    } else {
                        System.out.println(response);
                    }
                }

                @Override
                public void onCancelled() {

                }
            };
            client.sendHttp(request);
        }

        @Override
        public void onReceivePush(Client client, String content) {

        }

        @Override
        public void onKickUser(String deviceId, String userId) {

        }

    }
}