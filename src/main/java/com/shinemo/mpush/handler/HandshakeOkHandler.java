package com.shinemo.mpush.handler;

import com.shinemo.mpush.client.ClientConfig;
import com.shinemo.mpush.api.ClientListener;
import com.shinemo.mpush.api.Logger;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.connection.SessionStorage;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.message.HandshakeOkMessage;
import com.shinemo.mpush.security.AesCipher;
import com.shinemo.mpush.security.CipherBox;
import com.shinemo.mpush.session.PersistentSession;

/**
 * Created by ohun on 2016/1/23.
 */
public final class HandshakeOkHandler extends BaseMessageHandler<HandshakeOkMessage> {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public HandshakeOkMessage decode(Packet packet, Connection connection) {
        return new HandshakeOkMessage(packet, connection);
    }

    @Override
    public void handle(HandshakeOkMessage message) {
        Connection connection = message.getConnection();
        SessionContext context = connection.getSessionContext();
        byte[] serverKey = message.serverKey;
        if (serverKey.length != CipherBox.INSTANCE.getAesKeyLength()) {
            logger.w("handshake error serverKey invalid message=%s", message);
            connection.reconnect();
            return;
        }
        //设置心跳
        context.setHeartbeat(message.heartbeat);

        //更换密钥
        AesCipher cipher = (AesCipher) context.cipher;
        byte[] sessionKey = CipherBox.INSTANCE.mixKey(cipher.key, serverKey);
        context.changeCipher(new AesCipher(sessionKey, cipher.iv));

        //触发握手成功事件

        ClientListener listener = ClientConfig.I.getClientListener();
        listener.onHandshakeOk(connection.getClient(), message.heartbeat);

        //保存token
        saveToken(message, context);
        logger.w("<<< handshake ok message=%s, context=%s", message, context);
    }

    private void saveToken(HandshakeOkMessage message, SessionContext context) {
        SessionStorage storage = ClientConfig.I.getSessionStorage();
        if (storage == null || message.sessionId == null) return;
        PersistentSession session = new PersistentSession();
        session.sessionId = message.sessionId;
        session.expireTime = message.expireTime;
        session.cipher = context.cipher;
        storage.saveSession(PersistentSession.encode(session));
    }
}
