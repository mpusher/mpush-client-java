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



import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.message.ErrorMessage;
import com.mpush.client.ClientConfig;
import com.mpush.api.Logger;
import com.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ErrorMessageHandler extends BaseMessageHandler<ErrorMessage> {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public ErrorMessage decode(Packet packet, Connection connection) {
        return new ErrorMessage(packet, connection);
    }

    @Override
    public void handle(ErrorMessage message) {
        logger.w(">>> receive an error message=%s", message);
        if (message.cmd == Command.FAST_CONNECT.cmd) {
            ClientConfig.I.getSessionStorage().clearSession();
            message.getConnection().getClient().handshake();
        } else if (message.cmd == Command.HANDSHAKE.cmd) {
            message.getConnection().getClient().stop();
        } else {
            message.getConnection().reconnect();
        }
    }
}
