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
package com.mpush.client;

import com.mpush.api.Constants;
import com.mpush.api.Logger;
import com.mpush.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yxx on 2016/6/8.
 *
 * @author ohun@live.cn (夜色)
 */
/*package*/ class AllotClient {
    private List<String> lastServerAddress;
    private final ClientConfig config = ClientConfig.I;
    private final Logger logger = config.getLogger();

    public List<String> getServerAddress() {
        if (lastServerAddress == null || lastServerAddress.isEmpty()) {
            lastServerAddress = Arrays.asList(getServerAddressList());
        }

        return lastServerAddress;
    }

    public void removeBadAddress(String serverAddress) {
        lastServerAddress.remove(serverAddress);
    }

    public String[] getServerAddressList() {
        HttpURLConnection connection;
        try {
            URL url = new URL(config.getAllotServer());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                logger.w("get server address failure statusCode=%d", statusCode);
                connection.disconnect();
                return null;
            }
        } catch (IOException e) {
            logger.e(e, "get server address ex, when connect server.");
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(128);
        byte[] buffer = new byte[128];
        InputStream in = null;
        try {
            in = connection.getInputStream();
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        } catch (IOException ioe) {
            logger.e(ioe, "get server address ex, when read result.");
            return null;
        } finally {
            IOUtils.close(in);
            connection.disconnect();
        }
        byte[] content = out.toByteArray();
        if (content.length > 0) {
            String result = new String(content, Constants.UTF_8);
            logger.w("get server address success result=%s", result);
            return result.split(",");
        }
        logger.w("get server address failure return content empty.");
        return null;
    }
}
