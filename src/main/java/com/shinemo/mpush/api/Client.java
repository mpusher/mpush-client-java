package com.shinemo.mpush.api;

import com.shinemo.mpush.api.protocol.MPushProtocol;

/**
 * Created by ohun on 2016/1/17.
 */
public interface Client extends MPushProtocol {

    void start();

    void stop();

    void destroy();

    boolean isRunning();

}
