package com.mynger.mychatapp.model;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.mynger.mychatapp.util.ChannelUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Channel {
    public String senderUsername;
    public String recieverUsername;
    private PublicKey senderpubKey;
    private PrivateKey senderprivateKey;
    private PublicKey receiverpubKey;
    private PrivateKey receiverprivateKey;

    public Channel(String senderUsername, String recieverUsername) {
        this.senderUsername = senderUsername;
        this.recieverUsername = recieverUsername;
        setKeys();
    }

    public void setKeys() {
        try {
            KeyPair senderkeyPair = ChannelUtils.buildRSAKeyPair(); // Generate sender keys
            senderpubKey = senderkeyPair.getPublic();
            senderprivateKey = senderkeyPair.getPrivate();
            
            KeyPair receiverkeyPair = ChannelUtils.buildRSAKeyPair(); // Generate receiver keys
            receiverpubKey = receiverkeyPair.getPublic();
            receiverprivateKey = receiverkeyPair.getPrivate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
