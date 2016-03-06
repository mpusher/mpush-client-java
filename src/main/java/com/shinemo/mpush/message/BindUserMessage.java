package com.shinemo.mpush.message;


import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Created by ohun on 2015/12/28.
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
