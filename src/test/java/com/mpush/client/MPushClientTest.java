package com.mpush.client;

import java.util.concurrent.locks.LockSupport;

import com.mpush.api.Client;
import com.mpush.api.ClientListener;
import com.mpush.api.http.HttpCallback;
import com.mpush.api.http.HttpMethod;
import com.mpush.api.http.HttpRequest;
import com.mpush.api.http.HttpResponse;

/**
 * Created by ohun on 2016/1/25.
 */
public class MPushClientTest {
	private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
//	private static final String allocServer = "http://localhost:8080/webserver/cafe/getMPushServer.do";
	private static final String allocServer = "http://test.supplier.bicaijia.com/supplier/getMPushServer.do";

	public static void main(String[] args) throws Exception {
		String tokenDir = MPushClient.class.getResource("/").getPath();
		MPushClient.I.initClient(allocServer, publicKey, "11111", "Android", "5.0", "3.1", "10001", tokenDir,
				new L());
		MPushClient client = MPushClient.I;
		client.start();
//		Thread.sleep(2000);
//		client.handshake();
		
		Thread.sleep(3000);

		
//		client.sendMsg("HelloMessage111111", "user002", new R());

		LockSupport.park();
	}

	public static class R extends MPushCallback {

		@Override
		public void onSuccess(String userId, String content) {
			System.out.println("send message success with userId:" + userId + ", content:" + content);
		}

		@Override
		public void onFailure(String userId) {
			System.out.println("send message failed with userId:" + userId);
		}

		@Override
		public void onOffline(String userId) {
			System.out.println("send message but user Offline with userId:" + userId);
		}

		@Override
		public void onTimeout(String userId) {
			System.out.println("send message timeout with userId:" + userId);
		}

	}

	public static class L implements ClientListener {
		Thread thread;
		boolean flag = true;

		@Override
		public void onConnected(Client client) {
			System.out.println("========connect to MPush server success");
		}

		@Override
		public void onDisConnected(Client client) {
			System.out.println("========disconnect from server!");
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
			
			HttpRequest req = new HttpRequest(HttpMethod.POST, "http://127.0.0.1:8080/webserver/supplier/sendMessage.do");
			String para = "{\"srcUserAccountId\":1000,\"destUserAccountId\":10001,\"prevMessageId\":0,\"messageType\":1,\"content\":\"Hello World\",\"refMessageType\":1}";
			req.setBody(para.getBytes());
			req.setTimeout(100); // 必须要设置，否则立刻从队列中移除！！
			req.callback = new HttpCallback() {
				
				@Override
				public void onResponse(HttpResponse response) {
					if(response.statusCode == 200)
						System.out.println(new String(response.body));
				}
				
				@Override
				public void onCancelled() {
					// TODO Auto-generated method stub
					
				}
			};
			
			client.sendHttp(req);
		}

		@Override
		public void onReceivePush(Client client, String content) {
			System.out.println("===========receive a push message: " + content);
		}

		@Override
		public void onKickUser(String deviceId, String userId) {

		}

	}
}