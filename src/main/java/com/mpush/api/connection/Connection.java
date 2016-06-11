package com.mpush.api.connection;


import com.mpush.api.Client;
import com.mpush.api.protocol.Packet;

import java.nio.channels.SocketChannel;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Connection {

    void connect();

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
