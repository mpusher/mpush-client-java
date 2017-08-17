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

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mpush.api.Constants.DEFAULT_SO_TIMEOUT;

/**
 * Created by yxx on 2016/6/8.
 *
 * @author ohun@live.cn (夜色)
 */
/*package*/ final class AllotClient {
    private List<String> serverAddress = new ArrayList<>();

    public List<String> getServerAddress() {
        if (serverAddress.isEmpty()) {
            serverAddress = queryServerAddressList();
        }
        return serverAddress;
    }

    public List<String> queryServerAddressList() {
        ClientConfig config = ClientConfig.I;
        Logger logger = config.getLogger();

        if (config.getAllotServer() == null) {
            if (config.getServerHost() != null) {
                serverAddress.add(config.getServerHost() + ":" + config.getServerPort());
            }
            return serverAddress;
        }

        HttpURLConnection connection;
        try {
            URL url = new URL(config.getAllotServer());
            connection = (HttpURLConnection) url.openConnection();
            if (config.getAllotServer().startsWith("https")) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(getSSLSocketFactory());
                ((HttpsURLConnection) connection).setHostnameVerifier(new NullHostnameVerifier());
            }
            connection.setConnectTimeout(DEFAULT_SO_TIMEOUT);
            connection.setReadTimeout(DEFAULT_SO_TIMEOUT);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                logger.w("get server address failure statusCode=%d", statusCode);
                connection.disconnect();
                return serverAddress;
            }
        } catch (Exception e) {
            logger.e(e, "get server address ex, when connect server. allot=%s", config.getAllotServer());
            return Collections.emptyList();
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
            return serverAddress;
        } finally {
            IOUtils.close(in);
            connection.disconnect();
        }

        byte[] content = out.toByteArray();
        if (content.length > 0) {
            String result = new String(content, Constants.UTF_8);
            logger.w("get server address success result=%s", result);
            List<String> serverAddress = new ArrayList<>();
            serverAddress.addAll(Arrays.asList(result.split(",")));
            this.serverAddress = serverAddress;
        } else {
            logger.w("get server address failure return content empty.");
        }

        return serverAddress;
    }

    private SSLSocketFactory getSSLSocketFactory() {
        return getTrustAllContext().getSocketFactory();
    }

    private SSLContext getTrustAllContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new SecureRandom());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sslContext;
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class NullHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
