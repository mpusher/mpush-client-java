package com.mpush.api.protocol;

import com.mpush.api.http.HttpRequest;
import com.mpush.api.http.HttpResponse;
import com.mpush.client.MPushCallback;

import java.util.concurrent.Future;

/**
 * Created by ohun on 2016/1/17.
 */
public interface MPushProtocol {

    boolean healthCheck();

    void fastConnect();

    void handshake();

    void bindUser(String userId);

    void unbindUser();
    
    void sendMsg(String content, String destUserId, MPushCallback callback);

    Future<HttpResponse> sendHttp(HttpRequest request);
}
