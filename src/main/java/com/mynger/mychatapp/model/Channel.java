package com.mynger.mychatapp.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Channel {
    private String name;
    private Client sender;
    private Client reciever;

    public Channel(String senderUsername, String recieverUsername) {
        this.name = senderUsername + "-->" + recieverUsername;
        this.sender = new Client(senderUsername);
        this.reciever = new Client(recieverUsername);
    }
}
