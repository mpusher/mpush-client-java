package com.shinemo.mpush.api;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/23.
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");

    int DEFAULT_WRITE_TIMEOUT = 1000 * 10;//10s默认packet写超时

    byte[] EMPTY_BYTES = new byte[0];

    int DEF_HEARTBEAT = 4 * 60 * 1000;//5min 默认心跳时间

    int DEF_COMPRESS_LIMIT = 1024;//1k 启用压缩阈值

    String DEF_OS_NAME = "Android";//客户端OS

    int MAX_RESTART_COUNT = 10;//客户端重连次数超过该值，重连线程休眠10min后再重试
    int MAX_TOTAL_RESTART_COUNT = 1000;//客户端重连次数超过该值，将不再尝试重连

    int MAX_HB_TIMEOUT_COUNT = 2;

    String HTTP_HEAD_READ_TIMEOUT = "readTimeout";
}
