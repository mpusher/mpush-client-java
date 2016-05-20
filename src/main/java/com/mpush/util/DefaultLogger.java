package com.mpush.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mpush.api.Logger;

/**
 * Created by ohun on 2016/1/25.
 */
public final class DefaultLogger implements Logger {
    private String TAG = "[mpush] ";
    private final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    private boolean enable = true;
    
    public DefaultLogger(Class<?> clazz) {
		this.TAG = "[" + clazz.getSimpleName() + "] ";
	}
    
//    public DefaultLogger() {
//		this.TAG = "[mpush] ";
//	}
//
//    @Override
//    public void enable(boolean enabled) {
//        this.enable = enabled;
//    }

    @Override
    public void d(String s, Object... args) {
        if (enable) {
            System.out.printf(format.format(new Date()) + " [D] " + TAG + s + '\n', args);
        }
    }

    @Override
    public void i(String s, Object... args) {
        if (enable) {
            System.out.printf(format.format(new Date()) + " [I] " + TAG + s + '\n', args);
        }
    }

    @Override
    public void w(String s, Object... args) {
        if (enable) {
            System.err.printf(format.format(new Date()) + " [W] " + TAG + s + '\n', args);
        }
    }

    @Override
    public void e(Throwable e, String s, Object... args) {
        if (enable) {
            System.err.printf(format.format(new Date()) + " [E] " + TAG + s + '\n', args);
            e.printStackTrace();
        }
    }

}