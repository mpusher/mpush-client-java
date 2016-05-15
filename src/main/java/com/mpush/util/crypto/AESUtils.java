package com.mpush.util.crypto;


import com.mpush.client.ClientConfig;
import com.mpush.api.Constants;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ohun on 2015/12/25.
 */
public final class AESUtils {
    public static final String KEY_ALGORITHM = "AES";
    public static final String KEY_ALGORITHM_PADDING = "AES/CBC/PKCS5Padding";


    public static byte[] encrypt(byte[] data, byte[] encryptKey, byte[] iv) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(encryptKey, KEY_ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            return cipher.doFinal(data);
        } catch (Exception e) {
            ClientConfig.I.getLogger().e(e, "encrypt ex, decryptKey=%s", encryptKey);
        }
        return Constants.EMPTY_BYTES;
    }

    public static byte[] decrypt(byte[] data, byte[] decryptKey, byte[] iv) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(decryptKey, KEY_ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            return cipher.doFinal(data);
        } catch (Exception e) {
            ClientConfig.I.getLogger().e(e, "decrypt ex, decryptKey=%s", decryptKey);
        }
        return Constants.EMPTY_BYTES;
    }
}
