package com.mynger.mychatapp.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class FileUtils {
    public void saveKeyToFile(String filename, BigInteger modulus, BigInteger exp) throws Exception {
    FileOutputStream fos = null;
    ObjectOutputStream oos = null;
    try {
      fos = new FileOutputStream(filename);
      oos = new ObjectOutputStream(new BufferedOutputStream(fos));
      oos.writeObject(modulus);
      oos.writeObject(exp);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if(oos != null) oos.close();
      if(fos != null) fos.close();
    }
  }

  public void saveKeysToFile(PublicKey publicKey, PrivateKey privateKey) throws Exception {
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
    RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
    saveKeyToFile("pub.key", rsaPublicKeySpec.getModulus(), rsaPublicKeySpec.getPublicExponent());
    saveKeyToFile("pri.key", rsaPrivateKeySpec.getModulus(), rsaPrivateKeySpec.getPrivateExponent());
  }
}
