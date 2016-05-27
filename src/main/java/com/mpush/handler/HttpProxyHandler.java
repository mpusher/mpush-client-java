package com.mpush.handler;


import com.mpush.api.Logger;
import com.mpush.api.connection.Connection;
import com.mpush.api.http.HttpResponse;
import com.mpush.api.protocol.Packet;
import com.mpush.client.HttpRequestQueue;
import com.mpush.message.HttpResponseMessage;
import com.mpush.util.DefaultLogger;

/**
 * Created by ohun on 2015/12/30.
 */
public final class HttpProxyHandler extends BaseMessageHandler<HttpResponseMessage> {
	private static final Logger logger = new DefaultLogger(HttpProxyHandler.class);
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
        logger.d("<<< receive one response, sessionId=%d, statusCode=%d", message.getSessionId(), message.statusCode);
    }
}
