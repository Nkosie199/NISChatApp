package com.mynger.mychatapp.model;
import java.security.PublicKey;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PubKey {
    public String username;
    private PublicKey publicKey;
}
