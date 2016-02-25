package com.shinemo.mpush.handler;

import com.shinemo.mpush.api.Message;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;

public abstract class BaseMessageHandler<T extends Message> implements MessageHandler {
    public abstract T decode(Packet packet, Connection connection);

    public abstract void handle(T message);

    public void handle(Packet packet, Connection connection) {
        T t = decode(packet, connection);
        if (t != null) {
            handle(t);
        }
    }
}