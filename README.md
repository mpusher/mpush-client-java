## 介绍
#### mpush-client-java是一个纯java实现的一个MPUS客户端，不依赖其他任何第三方框架。

## 用途
#### 主要用于android sdk底层通信，该工程本身不包含任何android相关代码。

## 源码测试

参见 `com.mpush.client.MPushClientTest.java`
```java
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
                .setEnableHttpProxy(false)
                .setClientListener(new L())
                .create();
        client.start();

        LockSupport.park();
    }
```
