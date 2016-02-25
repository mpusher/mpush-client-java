package com.shinemo.mpush.handler;


import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.http.HttpCallback;
import com.shinemo.mpush.api.http.HttpResponse;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.client.ClientConfig;
import com.shinemo.mpush.client.HttpRequestQueue;
import com.shinemo.mpush.message.HttpResponseMessage;
import com.shinemo.mpush.message.OkMessage;

/**
 * Created by ohun on 2015/12/30.
 */
public final class HttpProxyHandler extends BaseMessageHandler<HttpResponseMessage> {
    private final Logger logger = ClientConfig.I.getLogger();
    private final HttpRequestQueue queue;

    public HttpProxyHandler(HttpRequestQueue queue) {
        this.queue = queue;
    }

    @Override
    public HttpResponseMessage decode(Packet packet, Connection connection) {
        return new HttpResponseMessage(packet, connection);
    }

    @Override
    public void handle(HttpResponseMessage message) {
        HttpRequestQueue.RequestTask task = queue.getAndRemove(message.getSessionId());
        if (task != null) {
            HttpResponse response = new HttpResponse(message.statusCode, message.reasonPhrase, message.headers, message.body);
            task.setResponse(response);
        }
        logger.d("receive one response, sessionId=%d, statusCode=%d", message.getSessionId(), message.statusCode);
    }
}
