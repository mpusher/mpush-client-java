package com.shinemo.mpush.api;

/**
 * Created by ohun on 2016/1/25.
 */
public interface Logger {

    void enable(boolean enabled);

    void d(String s, Object... args);

    void i(String s, Object... args);

    void w(String s, Object... args);

    void e(Throwable e, String s, Object... args);
}
