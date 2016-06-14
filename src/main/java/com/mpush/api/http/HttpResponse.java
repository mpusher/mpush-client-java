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
