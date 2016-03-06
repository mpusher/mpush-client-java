package com.shinemo.mpush.client;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.ClientListener;
import com.shinemo.mpush.util.DefaultLogger;
import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.*;

/**
 * Created by ohun on 2016/1/25.
 */
public class MPushClientTest {
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCcNLVG4noMfOIvKfV0eJAcADO4nr0hqoj42swL8DWY8CujpUGutw7Qk5LEn6i037wlF5CwIzJ7ix2xK+IcxEonOANtlS1NKbUXOCgUtA5mdZTnvAUByN0tzGp4BGywYNiXFQmLMXG5uxN0ZfcaoRKVqLzbcMnLB7VzS4L3OxzxqwIDAQAB";
    private static final String allocServer = "http://allot.mangguoyisheng.com/";

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
                .setEnableHttpProxy(false)
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
        }

        @Override
        public void onReceivePush(Client client, String content) {

        }

        @Override
        public void onKickUser(String deviceId, String userId) {

        }

    }
}