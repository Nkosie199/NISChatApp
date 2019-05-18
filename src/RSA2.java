import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import javax.crypto.*;

/**
 * RSA2
 */
public class RSA2 {

  private String publicKeyPath, privateKeyPath;
  private PublicKey publicKey;
  private PrivateKey privateKey;
  
  public RSA2(String pubKeyPath, String priKeyPath) throws NoSuchAlgorithmException {
    publicKeyPath = pubKeyPath;
    privateKeyPath = priKeyPath;
    generateKeyPairs();
  }  

  public void generateKeyPairs() throws NoSuchAlgorithmException {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair keyPair = kpg.generateKeyPair();
    publicKey = keyPair.getPublic();
    privateKey = keyPair.getPrivate();
  }

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
    RSA2 rsa2 = new RSA2("pub.key", "pri.key");

    if(args.length == 2){
      rsa2.loadPublicKey(args[0]);
      rsa2.loadPrivateKey(args[1]);
    }

    // SETUP KEYPAIR PARAMS (Key specs)
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(rsa2.getPublicKey(), RSAPublicKeySpec.class);
    RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory.getKeySpec(rsa2.getPrivateKey(), RSAPrivateKeySpec.class);

    // SAVE KEYS
    rsa2.saveKeys(rsa2.getPublicKeyPath(), rsaPublicKeySpec.getModulus(), rsaPublicKeySpec.getPublicExponent());
    rsa2.saveKeys(rsa2.getPrivateKeyPath(), rsaPrivateKeySpec.getModulus(), rsaPrivateKeySpec.getPrivateExponent());

    //CREATE MESSAGE
    String message = "This is top secret";
    System.out.println("original message: " + message);

    //ENCRYPT MESSAGE
    System.out.println("Encrypted message: " + new String(rsa2.encrypt(message)));    

    //DECRYPT MESSAGE
    System.out.println("Decrypted message: " + new String(rsa2.decrypt(rsa2.encrypt(message))));

  }

  public byte[] encrypt(String msg) {
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
  public String getPrivateKeyPath() { return privateKeyPath; }
  public String getPublicKeyPath() { return publicKeyPath; }
  public PrivateKey getPrivateKey() { return privateKey; }
  public PublicKey getPublicKey() { return publicKey; }

  public void saveKeys(String filename, BigInteger modulus, BigInteger exp) throws Exception {
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
}