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

package com.mpush.handler;


import com.mpush.api.ClientListener;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.api.Logger;
import com.mpush.client.ClientConfig;
import com.mpush.message.KickUserMessage;

/**
 * Created by ohun on 2016/1/23.
 *
 * @author ohun@live.cn (夜色)
 */
public final class KickUserHandler extends BaseMessageHandler<KickUserMessage> {
    private Logger logger = ClientConfig.I.getLogger();

    @Override
    public KickUserMessage decode(Packet packet, Connection connection) {
        return new KickUserMessage(packet, connection);
    }

    @Override
    public void handle(KickUserMessage message) {
        logger.w(">>> receive kickUser message=%s", message);
        ClientListener listener = ClientConfig.I.getClientListener();
        listener.onKickUser(message.deviceId, message.userId);
    }
}
