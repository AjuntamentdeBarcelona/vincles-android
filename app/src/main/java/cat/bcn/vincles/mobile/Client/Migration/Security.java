package cat.bcn.vincles.mobile.Client.Migration;

import android.util.Base64;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Security {

    private String publicKey = "d3m0p4ssw0rd";
    private SecretKey key = null;
    private int keyLength = 256;
    IvParameterSpec iv = new IvParameterSpec(new byte[16]);
    private byte[] salt = new byte[]
            {
                    0x07, 0x08, 0x00, 0x09, 0x06, 0x07, 0x09, 0x08, 0x08, 0x07,0x05, 0x06, 0x04, 0x05, 0x07, 0x06
            };

    ////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    ////				   GETTERS & SETTERS					\\\\

    ////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public void setPublicKey(String pass) {
        publicKey = pass;
    }

    public void setKeyLength(int len) {
        keyLength = len;
    }

    public void setSalt(byte[] newSalt) {
        salt = newSalt;
    }

    public synchronized static String toBase64(byte[] bytes) {
        return (Base64.encodeToString(bytes, Base64.NO_WRAP));
//        return (Base64.encodeToString(bytes, Base64.DEFAULT));
    }

    public synchronized static byte[] fromBase64(String base64Text) throws IOException {
        return (Base64.decode(base64Text, Base64.NO_WRAP));
//        return (Base64.decode(base64Text, Base64.DEFAULT));
    }

    public String getPublicKey() {
        return publicKey;
    }

    ////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    ////					 HASH MD5 ZONE						\\\\

    ////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public synchronized static byte[] hash(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("md5");
        return md.digest(text.getBytes("utf-8"));
    }

    public synchronized static String hashString(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("md5");
        return toBase64(md.digest(text.getBytes("utf-8")));
    }

    public synchronized static String md5(String s) {
        String ret = s;
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            ret = hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return ret;
    }



    ////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    ////						AES ZONE						\\\\

    ////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public String AESGenerateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHSHA256AND128BITAES-CBC-BC");
//    	SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(publicKey.toCharArray(), salt, 1024, keyLength);
        SecretKey tmp = factory.generateSecret(spec);
        key = new SecretKeySpec(tmp.getEncoded(), "AES");
        return toBase64(key.getEncoded());
    }

    public void AESLoadKey(String codedKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        key = null;
        key = new SecretKeySpec(fromBase64(codedKey), "AES");
    }

    public void loadPlainAESKey(String keys) {
        if (keys.length() > 24)
            keys = keys.substring(0, 24);
        key = new SecretKeySpec(keys.getBytes(), "AES");
    }

    public byte[] AESencrypt(String message) throws Exception {

        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        byte[] plainTextBytes = message.getBytes("utf-8");
        byte[] cipherText = cipher.doFinal(plainTextBytes);

        return cipherText;
    }

    public String AESdecrypt(byte[] message) throws Exception {

        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] plainText = decipher.doFinal(message);

        return new String(plainText, "UTF-8");
    }

}
