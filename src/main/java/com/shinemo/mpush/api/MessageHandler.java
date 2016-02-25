package com.shinemo.mpush.api;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2016/1/23.
 */
public interface MessageHandler {
    void handle(Packet packet, Connection connection);
}
