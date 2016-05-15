package com.mpush.api;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2016/1/17.
 */
public interface PacketReceiver {

    void onReceive(Packet packet, Connection connection);
}
