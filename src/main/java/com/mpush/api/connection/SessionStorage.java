package com.mpush.api.connection;

/**
 * Created by ohun on 2015/12/22.
 */
public interface SessionStorage {
    void saveSession(String sessionContext);

    String getSession();

    void clearSession();
}
