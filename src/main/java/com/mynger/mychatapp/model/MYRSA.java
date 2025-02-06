package com.mynger.mychatapp.model;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

/**
 * RSA
 */
public class MYRSA {

  Key pvt, pub;

  public MYRSA() {
  }

  /**
   * Generates Public private key pairs then writes them to file @throws Exception
   */
  public void generateKeyPair() throws Exception {
    //generate key pairs
    int keysize = 1024;
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(keysize);
    KeyPair keyPair = kpg.generateKeyPair();
    //outfile
    String outFile = "myrsa";
    
    //write out key pairs
    pvt = keyPair.getPrivate();
    Writer out = new FileWriter(outFile + ".key");
    Base64.Encoder encoder = Base64.getEncoder();
    out.write(encoder.encodeToString(pvt.getEncoded()));
    //log.info("Path of pvt key outfile: "+out);
    out.close();
    
    pub = keyPair.getPublic();
    out = new FileWriter(outFile + ".key");
    out.write(encoder.encodeToString(pub.getEncoded()));
    //log.info("Path of pub key outfile: "+out);
    out.close();
  }

  public void loadPvtKeyFromFile(String pvtKeyPath) throws Exception {
    /* Read all bytes from the private key file */
    Path path_to_pvt_key_file = Paths.get(pvtKeyPath);
    byte[] pvt_key_bytes = Files.readAllBytes(path_to_pvt_key_file);
    
    /* Generate private key. */
    PKCS8EncodedKeySpec pvt_key_spec = new PKCS8EncodedKeySpec(pvt_key_bytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    pvt = kf.generatePrivate(pvt_key_spec);
  }

  public void loadPubKeyFromFile(String pubKeyPath) throws Exception {
    /* Read all the public key bytes */
    Path path_to_pub_key_file = Paths.get(pubKeyPath);
    byte[] pub_key_bytes = Files.readAllBytes(path_to_pub_key_file);
    
    /* Generate public key. */
    X509EncodedKeySpec pub_key_spec = new X509EncodedKeySpec(pub_key_bytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    pub = kf.generatePublic(pub_key_spec);
  }
}