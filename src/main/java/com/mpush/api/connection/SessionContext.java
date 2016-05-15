package com.mpush.api.connection;

/**
 * Created by ohun on 2015/12/22.
 */
public final class SessionContext {
    public int heartbeat;
    public Cipher cipher;
    public String bindUser;

    public void changeCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void setBindUser(String bindUser) {
        this.bindUser = bindUser;
    }

    public boolean handshakeOk() {
        return heartbeat > 0;
    }

    @Override
    public String toString() {
        return "SessionContext{" +
                "heartbeat=" + heartbeat +
                ", cipher=" + cipher +
                ", bindUser='" + bindUser + '\'' +
                '}';
    }
}
