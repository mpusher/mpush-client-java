package com.shinemo.mpush.message;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.util.MPUtils;
import com.shinemo.mpush.util.WrappedByteBuffer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohun on 2016/2/15.
 */
public final class HttpResponseMessage extends ByteBufMessage {
    public int statusCode;
    public String reasonPhrase;
    public Map<String, String> headers;
    public byte[] body;

    public HttpResponseMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuffer body) {
        statusCode = decodeInt(body);
        reasonPhrase = decodeString(body);
        headers = MPUtils.headerFromString(decodeString(body));
        this.body = decodeBytes(body);
    }

    @Override
    public void encode(WrappedByteBuffer body) {
        encodeInt(body, statusCode);
        encodeString(body, reasonPhrase);
        encodeString(body, MPUtils.headerToString(headers));
        encodeBytes(body, this.body);
    }

    public static HttpResponseMessage from(HttpRequestMessage src) {
        return new HttpResponseMessage(src.createResponse(), src.connection);
    }

    public HttpResponseMessage setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public HttpResponseMessage setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
        return this;
    }

    public HttpResponseMessage addHeader(String name, String value) {
        if (headers == null) headers = new HashMap<>();
        this.headers.put(name, value);
        return this;
    }
}
