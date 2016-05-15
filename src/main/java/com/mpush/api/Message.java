package com.mpush.api;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2016/1/17.
 */
public interface Message {

    Connection getConnection();

    void send();

    void sendRaw();

    Packet getPacket();
}
