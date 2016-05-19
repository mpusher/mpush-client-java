package com.mpush.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import com.mpush.api.Logger;
import com.mpush.api.MessageHandler;
import com.mpush.api.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.handler.ErrorMessageHandler;
import com.mpush.handler.FastConnectOkHandler;
import com.mpush.handler.HandshakeOkHandler;
import com.mpush.handler.HeartbeatHandler;
import com.mpush.handler.KickUserHandler;
import com.mpush.handler.OkMessageHandler;
import com.mpush.handler.PushMessageHandler;
import com.mpush.util.DefaultLogger;
import com.mpush.util.thread.ExecutorManager;

/**
 * Created by ohun on 2016/1/20.
 */
public final class MessageDispatcher implements PacketReceiver {
    private final Executor executor = ExecutorManager.INSTANCE.getDispatchThread();
    private final Map<Byte, MessageHandler> handlers = new HashMap<>();
    private static final Logger logger = new DefaultLogger(MessageDispatcher.class);

    public MessageDispatcher() {
        register(Command.HEARTBEAT, new HeartbeatHandler());
        register(Command.FAST_CONNECT, new FastConnectOkHandler());
        register(Command.HANDSHAKE, new HandshakeOkHandler());
        register(Command.KICK, new KickUserHandler());
        register(Command.OK, new OkMessageHandler());
        register(Command.ERROR, new ErrorMessageHandler());
        register(Command.PUSH, new PushMessageHandler());
    }

    public void register(Command command, MessageHandler handler) {
        handlers.put(command.cmd, handler);
    }

    @Override
    public void onReceive(final Packet packet, final Connection connection) {
        final MessageHandler handler = handlers.get(packet.cmd);
        if (handler != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        handler.handle(packet, connection);
                    } catch (Throwable throwable) {
                        logger.e(throwable, "handle message error, packet=%s", packet);
                        connection.reconnect();
                    }
                }
            });
        } else {
            logger.w("<<< receive unsupported message, do reconnect, packet=%s", packet);
            connection.reconnect();
        }
    }
}
