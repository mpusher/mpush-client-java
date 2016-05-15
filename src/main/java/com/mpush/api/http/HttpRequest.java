package com.mpush.api.http;

import java.util.Map;

import static com.mpush.api.Constants.HTTP_HEAD_READ_TIMEOUT;

/**
 * Created by yxx on 2016/2/16.
 *
 * @author ohun@live.cn
 */
public final class HttpRequest {
    public final byte method;
    public final String uri;
    public Map<String, String> headers;
    public byte[] body;
    public HttpCallback callback;
    public int timeout;

    public HttpRequest(byte method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public static HttpRequest buildGet(String uri) {
        return new HttpRequest(HttpMethod.GET, uri);
    }

    public static HttpRequest buildPost(String uri) {
        return new HttpRequest(HttpMethod.POST, uri);
    }

    public static HttpRequest buildPut(String uri) {
        return new HttpRequest(HttpMethod.PUT, uri);
    }

    public static HttpRequest buildDelete(String uri) {
        return new HttpRequest(HttpMethod.DELETE, uri);
    }

    public static HttpRequest build(byte method, String uri) {
        return new HttpRequest(method, uri);
    }

    public byte getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpRequest setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpRequest setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public HttpCallback getCallback() {
        return callback;
    }

    public HttpRequest setCallback(HttpCallback callback) {
        this.callback = callback;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public HttpRequest setTimeout(int timeout) {
        this.timeout = timeout;
        if (headers != null) {
            headers.put(HTTP_HEAD_READ_TIMEOUT, Integer.toString(timeout));
        }
        return this;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "uri='" + uri + '\'' +
                ", method=" + method +
                ", timeout=" + timeout +
                '}';
    }
}
