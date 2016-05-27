package com.mpush.client;

import java.util.Map;

import com.mpush.api.Logger;
import com.mpush.api.http.HttpCallback;
import com.mpush.api.http.HttpResponse;
import com.mpush.util.DefaultLogger;

/**
 * 
 * @author maksimwei
 *
 */
public abstract class MPushCallback implements HttpCallback {
	private static final Logger logger = new DefaultLogger(MPushCallback.class);

	public abstract void onSuccess(String userId, String content);

	public abstract void onFailure(String userId);

	public abstract void onOffline(String userId);

	public abstract void onTimeout(String userId);

	@Override
	public void onResponse(HttpResponse response) {
//		String userId = "";
//		String content = "";
//
//		Map<String, String> header = response.headers;
//		if (header != null)
//			userId = header.get("USER_ID");
//		byte[] bodys = response.body;
//		if (bodys != null && bodys.length > 0)
//			content = response.body.toString();
		logger.d("receive a message:%s", response.toString());
		this.onSuccess("", "");
	}

	@Override
	public void onCancelled() {
		logger.w("call cancelled!!");
	}
}
