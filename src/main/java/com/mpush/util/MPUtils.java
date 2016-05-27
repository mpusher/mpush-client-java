package com.mpush.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohun on 2016/1/25.
 */
public final class MPUtils {

    public static String parseHost2Ip(String host) {
        InetAddress ia = null;
        try {
            ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
        }
        if (ia != null) {
            return ia.getHostAddress();
        }
        return host;
    }

    public static String headerToString(Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            StringBuilder sb = new StringBuilder(headers.size() * 64);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey())
                        .append(':')
                        .append(entry.getValue()).append('\n');
            }
            return sb.toString();
        }
        return null;
    }


    public static Map<String, String> headerFromString(String headersString) {
        if (headersString == null) return null;
        Map<String, String> headers = new HashMap<>();
        int L = headersString.length();
        String name, value = null;
        for (int i = 0, start = 0; i < L; i++) {
            char c = headersString.charAt(i);
            if (c != '\n') continue;
            if (start >= L - 1) break;
            String header = headersString.substring(start, i);
            start = i + 1;
            int index = header.indexOf(':');
            if (index <= 0) continue;
            name = header.substring(0, index);
            if (index < header.length() - 1) {
                value = header.substring(index + 1);
            }
            headers.put(name, value);
        }
        return headers;
    }
}
