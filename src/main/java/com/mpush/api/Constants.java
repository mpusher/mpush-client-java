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

package com.mpush.api;


import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn (夜色)
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");

    int DEFAULT_SO_TIMEOUT = 1000 * 3;//客户端连接超时时间

    int DEFAULT_WRITE_TIMEOUT = 1000 * 10;//10s默认packet写超时

    byte[] EMPTY_BYTES = new byte[0];

    int DEF_HEARTBEAT = 4 * 60 * 1000;//5min 默认心跳时间

    int DEF_COMPRESS_LIMIT = 1024;//1k 启用压缩阈值

    String DEF_OS_NAME = "android";//客户端OS

    int MAX_RESTART_COUNT = 10;//客户端重连次数超过该值，重连线程休眠10min后再重试
    int MAX_TOTAL_RESTART_COUNT = 1000;//客户端重连次数超过该值，将不再尝试重连

    int MAX_HB_TIMEOUT_COUNT = 2;

    String HTTP_HEAD_READ_TIMEOUT = "readTimeout";
}
