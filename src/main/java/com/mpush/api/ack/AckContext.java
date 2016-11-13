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

package com.mpush.api.ack;

import com.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2016/11/13.
 *
 * @author ohun@live.cn (夜色)
 */
public class AckContext {
    public AckCallback callback;
    public AckModel ackModel = AckModel.AUTO_ACK;
    public int timeout = 1000;
    public Packet request;
    public int retryCount;

    public static AckContext build(AckCallback callback) {
        AckContext context = new AckContext();
        context.setCallback(callback);
        return context;
    }

    public AckCallback getCallback() {
        return callback;
    }

    public AckContext setCallback(AckCallback callback) {
        this.callback = callback;
        return this;
    }

    public AckModel getAckModel() {
        return ackModel;
    }

    public AckContext setAckModel(AckModel ackModel) {
        this.ackModel = ackModel;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public AckContext setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Packet getRequest() {
        return request;
    }

    public AckContext setRequest(Packet request) {
        this.request = request;
        return this;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public AckContext setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }
}
