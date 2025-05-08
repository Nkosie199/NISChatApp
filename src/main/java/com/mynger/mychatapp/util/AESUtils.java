package com.mynger.mychatapp.util;

/**
 * AES
 */
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AESUtils {

  private static String ALGO = "AES";
  private byte[] keyValue;

  public AESUtils(String key) {
    keyValue = key.getBytes();
  }

  public static String encryptAES(String str, SecretKey key) throws Exception {
    Cipher ecipher = Cipher.getInstance("AES");
    ecipher.init(Cipher.ENCRYPT_MODE, key);
    // Encode the string into bytes using utf-8
    byte[] utf8 = str.getBytes("UTF8");
    // Encrypt
    byte[] enc = ecipher.doFinal(utf8);
    // Encode bytes to base64 to get a string
    return Base64.getEncoder().encodeToString(enc);
  }

  public String encrypt(String msg) throws Exception {
    Key key = new SecretKeySpec(keyValue, ALGO);
    Cipher cipher = Cipher.getInstance(ALGO);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] enc_msg = cipher.doFinal(msg.getBytes());

    Base64.Encoder encoder = Base64.getEncoder();
    String encoded_enc_msg = encoder.encodeToString(enc_msg);
    return encoded_enc_msg;
  }

  public static String decryptAES(String st, SecretKey key) throws Exception {
    Cipher dcipher = Cipher.getInstance("AES");
    dcipher.init(Cipher.DECRYPT_MODE, key);
    // Decode base64 to get bytes
    byte[] dec = Base64.getDecoder().decode(st);
    byte[] utf8 = dcipher.doFinal(dec);
    // Decode using utf-8
    return new String(utf8, "UTF8");
  }

  public String decrypt(String encoded_enc_msg) throws Exception {
    Key key = new SecretKeySpec(keyValue, ALGO);
    Cipher cipher = Cipher.getInstance(ALGO);
    cipher.init(Cipher.DECRYPT_MODE, key);
    byte[] enc_msg = Base64.getDecoder().decode(encoded_enc_msg);
    byte[] msg = cipher.doFinal(enc_msg);
    return new String(msg);
  }

  /**
   * @return the keyValue
   */
  public byte[] getKeyValue() {
    return keyValue;
  }
}