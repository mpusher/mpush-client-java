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
import com.mpush.util.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2015/12/28.
 *
 * @author ohun@live.cn (夜色)
 */
public final class BindUserMessage extends ByteBufMessage {
    public String userId;
    public String alias;
    public String tags;

    private BindUserMessage(Command cmd, Connection connection) {
        super(new Packet(cmd, genSessionId()), connection);
    }

    public static BindUserMessage buildBind(Connection connection) {
        return new BindUserMessage(Command.BIND, connection);
    }

    public static BindUserMessage buildUnbind(Connection connection) {
        return new BindUserMessage(Command.UNBIND, connection);
    }

    @Override
    public void decode(ByteBuffer body) {
        userId = decodeString(body);
        alias = decodeString(body);
        tags = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, userId);
        encodeString(body, alias);
        encodeString(body, tags);
    }

    public BindUserMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public BindUserMessage setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public BindUserMessage setTags(String tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public String toString() {
        return "BindUserMessage{" +
                "userId='" + userId + '\'' +
                ", alias='" + alias + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }
}
