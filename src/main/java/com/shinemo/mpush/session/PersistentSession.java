package com.shinemo.mpush.session;

import com.shinemo.mpush.api.connection.Cipher;
import com.shinemo.mpush.security.AesCipher;
import com.shinemo.mpush.util.Strings;

/**
 * Created by ohun on 2016/1/25.
 */
public final class PersistentSession {
    public String serverHost;
    public String sessionId;
    public long expireTime;
    public Cipher cipher;

    public boolean isExpired() {
        return expireTime < System.currentTimeMillis();
    }

    public static String encode(PersistentSession session) {
        return session.serverHost
                + "," + session.sessionId
                + "," + session.expireTime
                + "," + session.cipher.toString();
    }

    public static PersistentSession decode(String value) {
        String[] array = value.split(",");
        if (array.length != 5) return null;
        PersistentSession session = new PersistentSession();
        session.serverHost = array[0];
        session.sessionId = array[1];
        session.expireTime = Strings.toLong(array[2], 0);
        byte[] key = AesCipher.toArray(array[3]);
        byte[] iv = AesCipher.toArray(array[4]);
        if (key == null || iv == null) return null;
        session.cipher = new AesCipher(key, iv);
        return session;
    }

    @Override
    public String toString() {
        return "PersistentSession{" +
                "serverHost='" + serverHost + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", expireTime=" + expireTime +
                ", cipher=" + cipher +
                '}';
    }
}
