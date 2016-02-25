package com.shinemo.mpush.security;


import com.shinemo.mpush.api.connection.Cipher;
import com.shinemo.mpush.util.crypto.RSAUtils;

import java.security.interfaces.RSAPublicKey;

/**
 * Created by ohun on 2015/12/28.
 */
public final class RsaCipher implements Cipher {

    private final RSAPublicKey publicKey;

    public RsaCipher(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return RSAUtils.encryptByPublicKey(data, publicKey);
    }

    @Override
    public String toString() {
        return "RsaCipher [publicKey=" + new String(publicKey.getEncoded()) + "]";
    }

}
