package com.mynger.mychatapp.util;

import java.math.BigInteger;
import java.security.MessageDigest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HashingUtils {
    // Takes any string as input and calculates sha 512 bit hash. Output is in 128 bit hex string
    public static String sha512(String rawinput) {
        String hashout = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(rawinput.getBytes("utf8"));
            hashout = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception E) {
            log.info("Hash Exception");
        }
        return hashout;
    }
}
