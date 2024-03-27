package com.extractor.as400.util;

import com.extractor.as400.exceptions.AS400CipherException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to perform encrypt and decrypt actions
 */
public class CipherUtil {
    private static final String CLASSNAME = "CipherUtil";
    private static SecretKeySpec secretKey;
    private static IvParameterSpec iv;
    private static final String CIPHER_INSTANCE = "AES/CBC/PKCS5Padding";

    public static String AS_400_SEED_SECRET_KEY = "AS_400_SEED_SECRET_KEY";

    private static void setKey(String myKey) throws Exception {
        final String ctx = CLASSNAME + ".";
        try {
            byte[] salt = AS_400_SEED_SECRET_KEY.concat(myKey).getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            salt = sha.digest(salt);
            KeySpec spec = new PBEKeySpec(myKey.toCharArray(), salt, 65536, 128); // AES-256
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = f.generateSecret(spec).getEncoded();
            secretKey = new SecretKeySpec(key, "AES");
            iv = new IvParameterSpec(Arrays.copyOf(salt, 16));
        } catch (Exception e) {
            throw new Exception(ctx + ": " + e.getMessage());
        }
    }

    /**
     * Method used to encrypt and decrypt a string using a specific key
     * Use Cipher.ENCRYPT_MODE to encrypt
     * Use Cipher.DECRYPT_MODE to decrypt
     */
    public static String encryptionByMode(String str, String secret, int CIPHER_MODE) throws AS400CipherException {
        final String ctx = CLASSNAME + ".encryptionByMode";
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
            cipher.init(CIPHER_MODE, secretKey, iv);

            if (CIPHER_MODE != Cipher.ENCRYPT_MODE && CIPHER_MODE != Cipher.DECRYPT_MODE)
                throw new AS400CipherException("Error while encrypting or decrypting, the mode (" + CIPHER_MODE + ") is not supported.");

            if (CIPHER_MODE == Cipher.ENCRYPT_MODE)
                return Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));

            // Decrypt by default if mode is not Cipher.ENCRYPT_MODE
            return new String(cipher.doFinal(Base64.getDecoder().decode(str)));
        } catch (Exception e) {
            throw new AS400CipherException(ctx + ": " + e.getMessage());
        }
    }
}
