package com.example.securechat.Crypto;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Marty on 12/4/2017.
 */

public class ENCUtils {
    static KeyGenerator generator;
    private static Cipher cipher;
    private static Cipher decipher;
    private static SecretKey secretKeySpace;
    public static byte[] iv = new SecureRandom().generateSeed(16); public static final String KEY_ALIAS = "samplekeyalias"; //give any key alias based on your application. It is different from the key alias used to sign the app.
    public static final String RSA_MODE =  "RSA/ECB/PKCS1Padding"; // RSA algorithm which has to be used for OS version less than M
    public static KeyStore keyStore;
    public static KeyPairGenerator kpg;
    public static KeyPair geneateECKey(Context context) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        //ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("brainpoolp256r1");
        //Security.insertProviderAt(new BouncyCastleProvider(), 1);
        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp256r1");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "SC");
        kpg.initialize(ecParamSpec);
        KeyPair keyPair=kpg.generateKeyPair();
        return keyPair;
    }


    public static SecretKey generateSharedSecret(PrivateKey privateKey, PublicKey otherpublicKey) throws NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidKeySpecException, InvalidAlgorithmParameterException {

        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "SC");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(otherpublicKey, true);

            SecretKey key = keyAgreement.generateSecret("AES");
            Log.d("ENCUtils", "shared Key:"+key);
            return key;
        } catch (InvalidKeyException | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    public static String AESDescryptionMethod(SecretKey secretKey, String string) throws UnsupportedEncodingException {
        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secretKeySpace = new SecretKeySpec(secretKey.getEncoded(), "AES");
        byte[] ENcryptedByte = string.getBytes("ISO-8859-1");
        String decryptedString = string;
        byte[] decryption;
        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpace);
            Log.d("ENCUtils", "AES_DEC_Cipher:"+decipher);

            decryption = decipher.doFinal(ENcryptedByte);
            decryptedString = new String(decryption);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;

    }
    public static String AESEncryptionMethod(SecretKeySpec secretKeySpace, String string) {
        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];
         try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpace);
            Log.d("ENCUtils", "AES_Enc_Cipher:"+cipher.toString());
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        String returnString = null;
        try {
            returnString = new String(encryptedByte, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    public static String encryptString(SecretKey key, String plainText) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SC");
            byte[] plainTextBytes = plainText.getBytes("UTF-8");
            byte[] cipherText;
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            cipherText = new byte[cipher.getOutputSize(plainTextBytes.length)];
            int encryptLength = cipher.update(plainTextBytes, 0,
                    plainTextBytes.length, cipherText, 0);
            encryptLength += cipher.doFinal(cipherText, encryptLength);

            return bytesToHex(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | UnsupportedEncodingException | ShortBufferException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptString(SecretKey key, String cipherText) {
        try {
            Key decryptionKey = new SecretKeySpec(key.getEncoded(),
                    key.getAlgorithm());
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SC");
            byte[] cipherTextBytes = hexToBytes(cipherText);
            byte[] plainText;
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, ivSpec);
            plainText = new byte[cipher.getOutputSize(cipherTextBytes.length)];
            int decryptLength = cipher.update(cipherTextBytes, 0,
                    cipherTextBytes.length, plainText, 0);
            decryptLength += cipher.doFinal(plainText, decryptLength);
            return new String(plainText, "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException
                | ShortBufferException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytesToHex(byte[] data, int length) {
        String digits = "0123456789ABCDEF";
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i != length; i++) {
            int v = data[i] & 0xff;

            buffer.append(digits.charAt(v >> 4));
            buffer.append(digits.charAt(v & 0xf));
        }

        return buffer.toString();
    }

    public static String bytesToHex(byte[] data) {
        return bytesToHex(data, data.length);
    }

    public static byte[] hexToBytes(String string) {
        int length = string.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character
                    .digit(string.charAt(i + 1), 16));
        }
        return data;
    }


}