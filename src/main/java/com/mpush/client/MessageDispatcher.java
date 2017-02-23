/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.client;


import com.mpush.api.MessageHandler;
import com.mpush.api.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.handler.*;
import com.mpush.util.thread.ExecutorManager;
import com.mpush.api.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by ohun on 2016/1/20.
 *
 * @author ohun@live.cn (夜色)
 */
public final class MessageDispatcher implements PacketReceiver {
    private final Executor executor = ExecutorManager.INSTANCE.getDispatchThread();
    private final Map<Byte, MessageHandler> handlers = new HashMap<>();
    private final Logger logger = ClientConfig.I.getLogger();
    private final AckRequestMgr ackRequestMgr;

    public MessageDispatcher() {
        register(Command.HEARTBEAT, new HeartbeatHandler());
        register(Command.FAST_CONNECT, new FastConnectOkHandler());
        register(Command.HANDSHAKE, new HandshakeOkHandler());
        register(Command.KICK, new KickUserHandler());
        register(Command.OK, new OkMessageHandler());
        register(Command.ERROR, new ErrorMessageHandler());
        register(Command.PUSH, new PushMessageHandler());
        register(Command.ACK, new AckHandler());

        this.ackRequestMgr = AckRequestMgr.I();
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
                        doAckResponse(packet);
                        handler.handle(packet, connection);
                    } catch (Throwable throwable) {
                        logger.e(throwable, "handle message error, packet=%s", packet);
                        connection.reconnect();
                    }
                }
            });
        } else {
            logger.w("<<< receive unsupported message, packet=%s", packet);
            //connection.reconnect();
        }
    }

    private void doAckResponse(Packet packet) {
        AckRequestMgr.RequestTask task = ackRequestMgr.getAndRemove(packet.sessionId);
        if (task != null) {
            task.success(packet);
        }
    }
}
