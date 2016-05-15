package com.mpush.util;


import com.mpush.api.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by ohun on 2015/12/25.
 */
public final class IOUtils {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length / 4);
        DeflaterOutputStream zipOut = new DeflaterOutputStream(byteStream);
        try {
            zipOut.write(data);
            zipOut.finish();
            zipOut.close();
        } catch (IOException e) {
            return Constants.EMPTY_BYTES;
        } finally {
            close(zipOut);
        }
        return byteStream.toByteArray();
    }

    public static byte[] uncompress(byte[] data) {
        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            return Constants.EMPTY_BYTES;
        } finally {
            close(in);
        }
        return out.toByteArray();
    }
}
