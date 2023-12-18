package com.bracelet.ble.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES128 ECB mode PKCS7Padding
 * decrypted result is 256bit
 * -----Java considers PKCS5 and PKCS7 padding to be the "same"------
 */
public class AesECBUtils {
    /** algorithm/mode/padding **/
    private static final String CipherMode = "AES/ECB/PKCS5Padding";

    private static final byte[] DeviceKey = new byte[] {(byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78,
            (byte)0x9A, (byte)0xBC, (byte)0xDE, (byte)0xF0,
            (byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44,
            (byte)0x55, (byte)0x66, (byte)0x77, (byte)0x88};

    public static byte[] encrypt(byte[] input, byte[] key) {
        byte[] crypted = null;
        try {
            SecretKeySpec skey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            crypted = cipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return crypted;
    }

    public static byte[] decrypt(byte[] input, byte[] key) {
        byte[] output = null;
        try {
            SecretKeySpec skey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, skey);
            output = cipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static byte[] encryptByDeviceKey(byte[] input) {
        return encrypt(input, DeviceKey);
    }
}
