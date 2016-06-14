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

package com.mpush.message;


import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.util.MPUtils;
import com.mpush.util.ByteBuf;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by ohun on 2016/2/15.
 *
 * @author ohun@live.cn (夜色)
 */
public final class HttpRequestMessage extends ByteBufMessage {
    public byte method;
    public String uri;
    public Map<String, String> headers;
    public byte[] body;

    public HttpRequestMessage(Connection connection) {
        super(new Packet(Command.HTTP_PROXY, genSessionId()), connection);
    }

    public HttpRequestMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuffer body) {
        method = decodeByte(body);
        uri = decodeString(body);
        headers = MPUtils.headerFromString(decodeString(body));
        this.body = decodeBytes(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeByte(body, method);
        encodeString(body, uri);
        encodeString(body, MPUtils.headerToString(headers));
        encodeBytes(body, this.body);
    }



    public String getMethod() {
        switch (method) {
            case 0:
                return "GET";
            case 1:
                return "POST";
            case 2:
                return "PUT";
            case 3:
                return "DELETE";
        }
        return "GET";
    }
}
