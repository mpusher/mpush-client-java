package com.shinemo.mpush.api;

import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2016/1/17.
 */
public interface PacketWriter {
    void write(Packet packet);
}
