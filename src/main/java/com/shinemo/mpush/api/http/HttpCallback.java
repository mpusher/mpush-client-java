package com.shinemo.mpush.api.http;

/**
 * Created by yxx on 2016/2/16.
 *
 * @author ohun@live.cn
 */
public interface HttpCallback {

    void onResponse(HttpResponse response);

    void onCancelled();
}
