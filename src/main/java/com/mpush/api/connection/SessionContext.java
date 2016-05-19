package com.mpush.api.connection;

import com.mpush.client.MPushCallback;

/**
 * Created by ohun on 2015/12/22.
 */
public final class SessionContext {
    public int heartbeat;
    public Cipher cipher;
    public String bindUser;
    public String userId;
    public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public MPushCallback callBack;

    public MPushCallback getCallBack() {
		return callBack;
	}

	public void setCallBack(MPushCallback callBack) {
		this.callBack = callBack;
	}

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
