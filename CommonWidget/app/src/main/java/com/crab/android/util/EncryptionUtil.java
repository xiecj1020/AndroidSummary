package com.crab.android.util;

import android.content.Context;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密解密工具类
 */
public class EncryptionUtil {
    private static final String ASCII = "ASCII";
    private static final String MD5 = "MD5";
    private static final String AES = "AES";
    private static final String UTF8 = "UTF8";

    /**
     * 加密字符串并返回
     *
     * @param s 要加密的字符串
     * @return 加密的BASE 64字符串
     */
    public static String encrypt(final String s, final Context context) {
        if (s == null) {
            return null;
        }
        try {
            return base64(encode(s.getBytes(UTF8), context));
        } catch (final Exception e) {
            // e.printStackTrace();
        }
        return s;
    }

    /**
     * 解密字符串并返回
     *
     * @param s BASE 64加密的AES字符串
     * @return 解密字符串
     */
    public static String decrypt(final String s, final Context context) {
        if (s == null) {
            return s;
        }
        try {
            return new String(decode(decodeBase64(s.getBytes(UTF8)), context), UTF8);
        } catch (final Exception e) {
            // e.printStackTrace();
        }
        return s;
    }

    private static SecretKeySpec key(final Context context) {
        final String aes_key = "123456";
        try {
            //如何使用MD5进行散列以便始终获取128位的密钥，而不管密钥的字符串长度如何
            final MessageDigest digest = MessageDigest.getInstance(MD5);
            final byte[] b = digest.digest(aes_key.getBytes(ASCII));
            return new SecretKeySpec(b, AES);
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] encode(final byte[] src, final Context context) {
        try {
            final Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, key(context));
            return cipher.doFinal(src);
        } catch (final InvalidKeyException e) {
            e.printStackTrace();
        } catch (final BadPaddingException e) {
            e.printStackTrace();
        } catch (final IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (final NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 復号化
     */
    private static byte[] decode(final byte[] src, final Context context) {
        try {
            final Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, key(context));
            return cipher.doFinal(src);
        } catch (final InvalidKeyException e) {
            e.printStackTrace();
        } catch (final BadPaddingException e) {
            e.printStackTrace();
        } catch (final IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (final NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String base64(final byte[] b) {
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private static byte[] decodeBase64(final byte[] b) {
        return Base64.decode(b, Base64.DEFAULT);
    }
}
