package com.watsontv.mcchat;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class EncryptionTools {
    public static SecretKey getSharedSecret() throws IOException {
        SecretKey secretKey;
        try {
            KeyGenerator secretKeyGen = KeyGenerator.getInstance("AES");
            secretKeyGen.init(128);
            secretKey = secretKeyGen.generateKey();
        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to get shared secret key", e);
        }
        return secretKey;
    }

    /** Returns a byte array containing the cipher text */
    public static byte[] getEncrypted(Key key, byte[] plainText) throws IOException {
        try{
            Cipher cipher = Cipher.getInstance(key.getAlgorithm().equals("RSA") ? "RSA/ECB/PKCS1Padding" : "AES/CFB8/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainText);
        } catch (GeneralSecurityException e){
            throw new IOException("Failed to encrypt plain text", e);
        }
    }

    /** Returns a string containing the plain text */
    public static byte[] getDecrypted(SecretKey key, byte[] cipherText) throws IOException {
        try{
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            IvParameterSpec ivspec = new IvParameterSpec(key.getEncoded());
            cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
            return cipher.doFinal(cipherText);
        } catch (GeneralSecurityException e){
            throw new IOException("Failed to decrypt cipher text", e);
        }
    }

    /** Calculated a server ID hash given the base, public key and secret key */
    public static String getServerIdHash(String base, PublicKey publicKey, SecretKey secretKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(base.getBytes(StandardCharsets.ISO_8859_1));
            digest.update(secretKey.getEncoded());
            digest.update(publicKey.getEncoded());
            return new BigInteger(digest.digest()).toString(16);
        } catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException("Server ID hash algorithm unavailable.", e);
        }
    }
}
