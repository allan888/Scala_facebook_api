package com.xueliu;

/**
 * Created by xueliu on 12/14/15.
 */



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * RSA算法，实现数据的加密解密。
 * @author ShaoJiang
 *
 */
public class RSAUtil {

    public static String publicToString(PublicKey publicKey) {
        String publicString =  Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return publicString;
    }


    public static PublicKey stringToPublic(String key){
        try{
            byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static String encrypt(String s, PublicKey pub){
        Cipher cipher;
        try {

            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pub);

            byte[] bytes = cipher.doFinal(s.getBytes());
            String encryptedString = Base64.getEncoder().encodeToString(bytes);
            return encryptedString;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encryptWithPrivate(String s, PrivateKey priv){
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, priv);
            byte[] bytes = cipher.doFinal(s.getBytes());
            String encryptedString = Base64.getEncoder().encodeToString(bytes);
            return encryptedString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptWithPublic(String s, PublicKey pub){
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, pub);

            byte[] bytes = Base64.getDecoder().decode(s);

            String ret = new String(cipher.doFinal(bytes));

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String s, PrivateKey priv){
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priv);

            byte[] bytes = Base64.getDecoder().decode(s);

            String ret = new String(cipher.doFinal(bytes));

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String aes_encrypt(String key, String str) {
        try {
            String initVector = "RandomInitVector";
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(str.getBytes());

            String encryptedString = Base64.getEncoder().encodeToString(encrypted);

            return encryptedString;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String aes_decrypt(String key,  String str) {
        try {
            String initVector = "RandomInitVector";
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] bytes = Base64.getDecoder().decode(str);
            byte[] original = cipher.doFinal(bytes);

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
