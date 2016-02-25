package com.shinemo.mpush.client;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.util.DefaultLogger;
import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.*;

/**
 * Created by ohun on 2016/1/25.
 */
public class MPushClientTest {
    private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
    private String allocServer = "http://allot.mangguoyisheng.com/";

    @Test
    public void testStart() throws Exception {
        Client client = ClientConfig
                .build()
                .setPublicKey(publicKey)
                .setAllotServer(allocServer)
                .setDeviceId("1111111111")
                .setOsName("Android")
                .setOsVersion("6.0")
                .setClientVersion("2.0")
                .setUserId("doctor43test")
                .setSessionStorageDir(getClass().getResource("/").getFile())
                .setLogger(new DefaultLogger())
                .setLogEnabled(true)
                .setEnableHttpProxy(false)
                .create();
        client.start();
        LockSupport.park();
    }
}