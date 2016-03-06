package com.shinemo.mpush.client;

import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.handler.*;
import com.shinemo.mpush.util.thread.ExecutorManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by ohun on 2016/1/20.
 */
public final class MessageDispatcher implements PacketReceiver {
    private final Executor executor = ExecutorManager.INSTANCE.getDispatchThread();
    private final Map<Byte, MessageHandler> handlers = new HashMap<>();
    private final Logger logger = ClientConfig.I.getLogger();

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
