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
