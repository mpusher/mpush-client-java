package com.shinemo.mpush.api.connection;


import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.protocol.Packet;

import java.nio.channels.SocketChannel;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Connection {

    SessionContext getSessionContext();

    void send(Packet packet);

    void close();

    boolean isConnected();

    void reconnect();

    boolean isReadTimeout();

    boolean isWriteTimeout();

    void setLastReadTime();

    void setLastWriteTime();

    SocketChannel getChannel();

    Client getClient();

}
