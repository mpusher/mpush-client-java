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

package com.mpush.util;


import com.mpush.api.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ohun on 2016/1/25.
 *
 * @author ohun@live.cn (夜色)
 */
public final class DefaultLogger implements Logger {
    private static final String TAG = "[mpush] ";
    private final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    private boolean enable = false;

    @Override
    public void enable(boolean enabled) {
        this.enable = enabled;
    }

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
