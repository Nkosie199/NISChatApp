package com.mynger.mychatapp.model;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.mynger.mychatapp.util.ChannelUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@NoArgsConstructor
public class Client {
  private String username;
  private PublicKey publicKey;
  private PrivateKey privateKey;

  public Client(String username) {
    this.username = username;
    setKeys();
  }

  public void setKeys() {
      try {
          KeyPair keyPair = ChannelUtils.buildRSAKeyPair();
          publicKey = keyPair.getPublic();
          privateKey = keyPair.getPrivate();
      } catch (Exception ex) {
          log.error(ex.getMessage());
      }
  }

  public PublicKey getPublicKey() {
      return publicKey;
  }
}
