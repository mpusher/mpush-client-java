package com.mpush.api.http;

import com.mpush.api.Constants;

import java.util.Map;

/**
 * Created by yxx on 2016/2/16.
 *
 * @author ohun@live.cn
 */
public final class HttpResponse {
    public final int statusCode;
    public final String reasonPhrase;
    public final Map<String, String> headers;
    public final byte[] body;

    public HttpResponse(int statusCode, String reasonPhrase, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                ", headers=" + headers +
                ", body=" + (body == null ? "" : new String(body, Constants.UTF_8)) +
                '}';
    }
}
