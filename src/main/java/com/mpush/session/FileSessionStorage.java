package com.mpush.session;

import com.mpush.api.connection.SessionStorage;
import com.mpush.util.IOUtils;
import com.mpush.api.Constants;
import com.mpush.client.ClientConfig;

import java.io.*;

/**
 * Created by ohun on 2016/1/25.
 */
public final class FileSessionStorage implements SessionStorage {
    private final String rootDir;
    private final String fileName = "token.dat";

    public FileSessionStorage(String rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void saveSession(String sessionContext) {
        File file = new File(rootDir, fileName);
        FileOutputStream out = null;
        try {
            if (!file.exists()) file.getParentFile().mkdirs();
            else if (file.canWrite()) file.delete();
            out = new FileOutputStream(file);
            out.write(sessionContext.getBytes(Constants.UTF_8));
        } catch (Exception e) {
            ClientConfig.I.getLogger().e(e, "save session context ex, session=%s, rootDir=%s"
                    , sessionContext, rootDir);
        } finally {
            IOUtils.close(out);
        }
    }

    @Override
    public String getSession() {
        File file = new File(rootDir, fileName);
        if (!file.exists()) return null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            if (bytes.length > 0) {
                in.read(bytes);
                return new String(bytes, Constants.UTF_8);
            }
            in.close();
        } catch (Exception e) {
            ClientConfig.I.getLogger().e(e, "get session context ex,rootDir=%s", rootDir);
        } finally {
            IOUtils.close(in);
        }
        return null;
    }

    @Override
    public void clearSession() {
        File file = new File(rootDir, fileName);
        if (file.exists() && file.canWrite()) {
            file.delete();
        }
    }
}
