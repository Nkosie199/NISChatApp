package com.mynger.mychatapp.model;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.*;

import lombok.extern.slf4j.Slf4j;

/**
 * RSA2
 * contains public and private keys used by user
 */
@Slf4j
public class RSA2 {

  private PublicKey publicKey;
  private PrivateKey privateKey;
  
  /**
   * ctor loads key files from files passed in as args
   * if keys could not be loaded, it generates them
   * @param pubKeyPath
   * @param priKeyPath
   * @throws NoSuchAlgorithmException
   */
  public RSA2(String pubKeyPath, String priKeyPath) throws NoSuchAlgorithmException {
    try{
      loadPublicKey(pubKeyPath);
      loadPrivateKey(priKeyPath);
    } catch (Exception e){
      log.info("Could not load files. \nGenerating new keys...");
      generateKeyPairs();
    }
  }

  /**
   * generates keys randomly
   * @throws Exception
   */
  public RSA2() throws Exception {
    generateKeyPairs();
  }

  /**
   * generates key pairs with keysize 2048 using RSA
   * @throws NoSuchAlgorithmException
   */
  public void generateKeyPairs() throws NoSuchAlgorithmException {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair keyPair = kpg.generateKeyPair();
    publicKey = keyPair.getPublic();
    privateKey = keyPair.getPrivate();
  }

  /**
   * loads public key from raw files 
   * @param filename
   */
  public void loadPublicKey(String filename) {
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    try {
      fis = new FileInputStream(new File(filename));
      ois = new ObjectInputStream(fis);
      //load mod and exp objects
      BigInteger modulus = (BigInteger) ois.readObject();
      BigInteger exponent = (BigInteger) ois.readObject();

      RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      publicKey = keyFactory.generatePublic(rsaPublicKeySpec);
      
    } catch (Exception e) {
      //TODO: handle exception
      e.printStackTrace();
    }
  }

  /**
   * loads pirvate key from raw file
   * @param filename
   */
  public void loadPrivateKey(String filename) {
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    try {
      fis = new FileInputStream(new File(filename));
      ois = new ObjectInputStream(fis);

      //load mod and exp objects
      BigInteger modulus = (BigInteger) ois.readObject();
      BigInteger exponent = (BigInteger) ois.readObject();

      RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(modulus, exponent);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      privateKey = keyFactory.generatePrivate(rsaPrivateKeySpec);
      
    } catch (Exception e) {
      //TODO: handle exception
      e.printStackTrace();
    }
    
  }

  public static void main(String[] args) throws Exception {
    //GENERATE PUBLIC KEY
    RSA2 rsa2 = null;

    if(args.length == 2){ // load from file
      rsa2.loadPublicKey(args[0]);
      rsa2.loadPrivateKey(args[1]);
    } else { // generate new keys
      rsa2 = new RSA2();
    }

    

    //CREATE MESSAGE
    String message = "This is top secret";
    log.info("original message: " + message);

    //ENCRYPT MESSAGE using recipient's pub key then send
    PublicKey bobPublicKey = rsa2.getPublicKey();
    byte[] encMsgBytes = rsa2.encrypt(message, bobPublicKey);
    log.info("Encrypted message: " + new String(encMsgBytes));    

    //SIGN MESSAGE
    byte[] msgSignature = rsa2.sign(encMsgBytes);

    log.info("Check signature: " + rsa2.verify(encMsgBytes,msgSignature));    

    //DECRYPT MESSAGE
    log.info("Decrypted message: " + new String(rsa2.decrypt(encMsgBytes)));

  }

  /**
   * signs incoming data
   * @param data
   * @return the message signature for the data
   * @throws Exception
   */
  public byte[] sign(byte[] data) throws Exception {
    //CREATE SIGNATURE
    Signature signature = Signature.getInstance("SHA1withRSA");

    //SIGN MESSAGE
    signature.initSign(privateKey);
    signature.update(data);
    return signature.sign();
  }

  /**
   * checks if the data has been signed with a given private key by 
   * using the public key associated with it.
   * @param data
   * @param msgSignature
   * @return the validation status of the data-signature pair
   * @throws Exception
   */
  public boolean verify(byte[] data, byte[] msgSignature) throws Exception {
    //CREATE SIGNATURE
    Signature signature = Signature.getInstance("SHA1withRSA");

    //VERIFY MESSAGE
    signature.initVerify(publicKey);
    signature.update(data);
    return signature.verify(msgSignature);
  }

  /**
   * encrypts given string using the intended recipient's public key
   * @param msg
   * @return
   */
  public byte[] encrypt(String msg, PublicKey publicKey) {
    //ENCRYPTING msg

    byte[] msgBytes = msg.getBytes();
    byte[] encMsgBytes = null;
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      encMsgBytes = cipher.doFinal(msgBytes);
      //DATA ENCRYPTED

      //---- use if bytes cause issues

      //ENCODED ENRYPTED MESSAGE
      // Base64.Encoder encoder = Base64.getEncoder();
      // String encMsgString =  encoder.encodeToString(encMsgBytes);
      //------- end if -------------
    } catch (Exception e) {
      e.printStackTrace();
    }
    return encMsgBytes;
  }

  public byte[] decrypt(byte[] encMsgBytes) {
    //ENCRYPTING msg

    byte[] msgBytes = null;
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      msgBytes = cipher.doFinal(encMsgBytes);
      //DATA ENCRYPTED
    } catch (Exception e) {
      e.printStackTrace();
    }
    return msgBytes;
  }

  /* GETTERS */
  public PrivateKey getPrivateKey() { return privateKey; }
  public PublicKey getPublicKey() { return publicKey; }

  public void saveKey(String filename, BigInteger modulus, BigInteger exp) throws Exception {
    FileOutputStream fos = null;
    ObjectOutputStream oos = null;
    try {
      // SAVE
      fos = new FileOutputStream(filename);
      oos = new ObjectOutputStream(new BufferedOutputStream(fos));
      oos.writeObject(modulus);
      oos.writeObject(exp);
    } catch (Exception e) {
      //TODO: handle exception
      e.printStackTrace();
    } finally {
      if(oos != null) oos.close();
      if(fos != null) fos.close();
    }
  }

  public void saveKeys() throws Exception {
    // SETUP KEYPAIR PARAMS (Key specs)
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
    RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);

    saveKey("pub.key", rsaPublicKeySpec.getModulus(), rsaPublicKeySpec.getPublicExponent());
    saveKey("pri.key", rsaPrivateKeySpec.getModulus(), rsaPrivateKeySpec.getPrivateExponent());
  }
}