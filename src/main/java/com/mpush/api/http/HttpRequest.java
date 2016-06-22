/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.api.http;


import com.mpush.api.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static com.mpush.api.Constants.HTTP_HEAD_READ_TIMEOUT;

/**
 * Created by yxx on 2016/2/16.
 *
 * @author ohun@live.cn
 */
public final class HttpRequest {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=";
    public final byte method;
    public final String uri;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body;
    private HttpCallback callback;
    private int timeout;

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
        headers.put(HTTP_HEAD_READ_TIMEOUT, Integer.toString(timeout));
        return headers;
    }

    public HttpRequest setHeaders(Map<String, String> headers) {
        this.getHeaders().putAll(headers);
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpRequest setBody(byte[] body, String contentType) {
        this.body = body;
        this.headers.put(CONTENT_TYPE, contentType);
        return this;
    }

    public HttpRequest setPostParam(Map<String, String> headers, Charset paramsEncoding) {
        byte[] bytes = encodeParameters(headers, paramsEncoding.name());
        setBody(bytes, CONTENT_TYPE_FORM + paramsEncoding.name());
        return this;
    }

    public HttpRequest setPostParam(Map<String, String> headers) {
        setPostParam(headers, Constants.UTF_8);
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
        return this;
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
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
