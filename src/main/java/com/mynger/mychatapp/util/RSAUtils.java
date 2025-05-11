package com.mynger.mychatapp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RSAUtils {
  // 0: private
  // 1: public
  public static String encryptRSAWithPubkey(
      PublicKey publicKey,
      String message) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] utf8 = cipher.doFinal(message.getBytes("UTF-8"));
    return Base64.getEncoder().encodeToString(utf8);
  }

  public static String encryptRSAWithPrivatekey(
      PrivateKey privateKey,
      String message) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);
    byte[] utf8 = cipher.doFinal(message.getBytes("UTF-8"));
    return Base64.getEncoder().encodeToString(utf8);
  }

  public byte[] encrypt(String msg, PublicKey publicKey) {
    byte[] msgBytes = msg.getBytes();
    byte[] encMsgBytes = null;
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      encMsgBytes = cipher.doFinal(msgBytes);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return encMsgBytes;
  }

  public static String decryptRSAWithPubKey(
      PublicKey publicKey,
      String st) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    byte[] encrypted = Base64.getDecoder().decode(st);
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    byte[] utf8 = cipher.doFinal(encrypted);
    return new String(utf8, "UTF8");
  }

  public static String decryptRSAWithPrivateKey(
      PrivateKey privateKey,
      String st) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    byte[] encrypted = Base64.getDecoder().decode(st);
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] utf8 = cipher.doFinal(encrypted);
    return new String(utf8, "UTF8");
  }

  public byte[] decrypt(PrivateKey privateKey, byte[] encMsgBytes) {
    byte[] msgBytes = null;
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      msgBytes = cipher.doFinal(encMsgBytes);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return msgBytes;
  }

  public Key loadPvtKeyFromFile(String pvtKeyPath) throws Exception {
    /* Read all bytes from the private key file */
    Path path_to_pvt_key_file = Paths.get(pvtKeyPath);
    byte[] pvt_key_bytes = Files.readAllBytes(path_to_pvt_key_file);

    /* Generate private key. */
    PKCS8EncodedKeySpec pvt_key_spec = new PKCS8EncodedKeySpec(pvt_key_bytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    Key pvt = kf.generatePrivate(pvt_key_spec);
    return pvt;
  }

  public Key loadPubKeyFromFile(String pubKeyPath) throws Exception {
    /* Read all the public key bytes */
    Path path_to_pub_key_file = Paths.get(pubKeyPath);
    byte[] pub_key_bytes = Files.readAllBytes(path_to_pub_key_file);

    /* Generate public key. */
    X509EncodedKeySpec pub_key_spec = new X509EncodedKeySpec(pub_key_bytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    Key pub = kf.generatePublic(pub_key_spec);
    return pub;
  }

  // -----------------------------------------------------------------------------------------

  public KeyPair generateKeyPairs() throws NoSuchAlgorithmException {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair keyPair = kpg.generateKeyPair();
    return keyPair;
  }

  public PublicKey getPublicKey(String filename) throws IOException {
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    PublicKey publicKey = null;
    try {
      fis = new FileInputStream(new File(filename));
      ois = new ObjectInputStream(fis);
      BigInteger modulus = (BigInteger) ois.readObject();
      BigInteger exponent = (BigInteger) ois.readObject();
      RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      publicKey = keyFactory.generatePublic(rsaPublicKeySpec);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (ois != null)
        ois.close();
      if (fis != null)
        fis.close();
    }
    return publicKey;
  }

  public PrivateKey getPrivateKey(String filename) throws IOException {
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    PrivateKey privateKey = null;
    try {
      fis = new FileInputStream(new File(filename));
      ois = new ObjectInputStream(fis);
      BigInteger modulus = (BigInteger) ois.readObject();
      BigInteger exponent = (BigInteger) ois.readObject();
      RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(modulus, exponent);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      privateKey = keyFactory.generatePrivate(rsaPrivateKeySpec);

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (ois != null)
        ois.close();
      if (fis != null)
        fis.close();
    }
    return privateKey;
  }

  public void test(PublicKey publicKey, PrivateKey privateKey) throws Exception {
    String message = "This is top secret";
    log.info("original message: " + message);
    byte[] encMsgBytes = encrypt(message, publicKey);
    log.info("Encrypted message: " + new String(encMsgBytes));
    byte[] msgSignature = sign(privateKey, encMsgBytes);
    log.info("Check signature: " + verify(publicKey, encMsgBytes, msgSignature));
    log.info("Decrypted message: " + new String(decrypt(privateKey, encMsgBytes)));
  }

  public byte[] sign(PrivateKey privateKey, byte[] data) throws Exception {
    Signature signature = Signature.getInstance("SHA1withRSA");
    signature.initSign(privateKey);
    signature.update(data);
    return signature.sign();
  }

  public boolean verify(PublicKey publicKey, byte[] data, byte[] msgSignature) throws Exception {
    Signature signature = Signature.getInstance("SHA1withRSA");
    signature.initVerify(publicKey);
    signature.update(data);
    return signature.verify(msgSignature);
  }
}