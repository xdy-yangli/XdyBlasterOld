package com.example.xdyblaster.util;


import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class AndroidDes3Util {

    private static final String ALGORITHM = "DESede";

    /**

     * 加解密统一使用的编码方式

     */

    private final static String encoding = "utf-8";



    /**

     * 3DES加密

     *

     * @param plainText 普通文本

     * @return

     */

    public static String encode(String plainText, String secretKey) throws Exception {

        Key deskey = null;

        DESedeKeySpec spec = new DESedeKeySpec(secretKey.getBytes());

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);

        deskey = keyFactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance(ALGORITHM);

        cipher.init(Cipher.ENCRYPT_MODE, deskey);

        byte[] encryptData = cipher.doFinal(plainText.getBytes(encoding));

        return Base64.encodeToString(encryptData,Base64.NO_WRAP);
     //           Base64.encodeToString(encryptData, Base64.DEFAULT);

    }



    /**

     * 3DES解密

     *

     * @param encryptText 加密文本

     * @param secretKey   密钥

     * @return

     */

    public static String decode(String encryptText, String secretKey) throws Exception {

        Key deskey = null;

        DESedeKeySpec spec = new DESedeKeySpec(secretKey.getBytes());

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);

        deskey = keyFactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance(ALGORITHM);

        cipher.init(Cipher.DECRYPT_MODE, deskey);

        byte[] decryptData = cipher.doFinal(Base64.decode(encryptText, Base64.DEFAULT));

        return new String(decryptData, encoding);

    }

}
