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
