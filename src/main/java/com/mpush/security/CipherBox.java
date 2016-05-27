package com.mpush.security;


import com.mpush.client.ClientConfig;
import com.mpush.util.crypto.RSAUtils;

import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by ohun on 2015/12/24.
 */
public final class CipherBox {
    public int aesKeyLength = ClientConfig.I.getAesKeyLength();
    public static final CipherBox INSTANCE = new CipherBox();
    private SecureRandom random = new SecureRandom();
    private RSAPublicKey publicKey;


    public RSAPublicKey getPublicKey() {
        if (publicKey == null) {
            String key = ClientConfig.I.getPublicKey();
            try {
                publicKey = (RSAPublicKey) RSAUtils.decodePublicKey(key);
            } catch (Exception e) {
                throw new RuntimeException("load public key ex, key=" + key, e);
            }
        }
        return publicKey;
    }

    public byte[] randomAESKey() {
        byte[] bytes = new byte[aesKeyLength];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] randomAESIV() {
        byte[] bytes = new byte[aesKeyLength];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] mixKey(byte[] clientKey, byte[] serverKey) {
        byte[] sessionKey = new byte[aesKeyLength];
        for (int i = 0; i < aesKeyLength; i++) {
            byte a = clientKey[i];
            byte b = serverKey[i];
            int sum = Math.abs(a + b);
            int c = (sum % 2 == 0) ? a ^ b : b ^ a;
            sessionKey[i] = (byte) c;
        }
        return sessionKey;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }

    public RsaCipher getRsaCipher() {
        return new RsaCipher(getPublicKey());
    }
}
