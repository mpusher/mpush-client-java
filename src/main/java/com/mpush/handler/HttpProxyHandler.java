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

package com.mpush.handler;


import com.mpush.api.connection.Connection;
import com.mpush.api.http.HttpResponse;
import com.mpush.api.protocol.Packet;
import com.mpush.client.HttpRequestMgr;
import com.mpush.api.Logger;
import com.mpush.client.ClientConfig;
import com.mpush.message.HttpResponseMessage;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn (夜色)
 */
public final class HttpProxyHandler extends BaseMessageHandler<HttpResponseMessage> {
    private final Logger logger = ClientConfig.I.getLogger();
    private final HttpRequestMgr httpRequestMgr;

    public HttpProxyHandler() {
        this.httpRequestMgr = HttpRequestMgr.I();
    }

    @Override
    public HttpResponseMessage decode(Packet packet, Connection connection) {
        return new HttpResponseMessage(packet, connection);
    }

    @Override
    public void handle(HttpResponseMessage message) {
        HttpRequestMgr.RequestTask task = httpRequestMgr.getAndRemove(message.getSessionId());
        if (task != null) {
            HttpResponse response = new HttpResponse(message.statusCode, message.reasonPhrase, message.headers, message.body);
            task.setResponse(response);
        }
        logger.d(">>> receive one response, sessionId=%d, statusCode=%d", message.getSessionId(), message.statusCode);
    }
}
