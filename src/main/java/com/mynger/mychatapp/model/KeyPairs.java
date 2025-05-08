package com.mynger.mychatapp.model;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class KeyPairs {
    public String user;
    private PublicKey senderpubKey;
    private PrivateKey senderprivateKey;
    private PublicKey receiverpubKey;
    private PrivateKey receiverprivateKey;

    public KeyPairs(String username) {
        user = username;
        try {
            // Generating sender keys
            KeyPair senderkeyPair = buildKeyPair();
            senderpubKey = senderkeyPair.getPublic();
            senderprivateKey = senderkeyPair.getPrivate();
            // Generating receiver keys
            KeyPair receiverkeyPair = buildKeyPair();
            receiverpubKey = receiverkeyPair.getPublic();
            receiverprivateKey = receiverkeyPair.getPrivate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }
}
