package com.mynger.mychatapp.util;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SenderUtils {
    public static String user;
    private static PublicKey senderpubKey;
    private static PrivateKey senderprivateKey;
    private static PublicKey receiverpubKey;
    private static ArrayList<String> logMessages = new ArrayList<>();

    public static void sendMessageToServer(String msg2server) throws Exception {
        // 1) Writes to server; PGP hashing and encryption should occur here...
        // 2) Generating SHA-512 hash of original message
        String hashedMsg2Server = HashingUtils.sha512(msg2server);
        // 3) Encrypt the message hash with sender private keys -> Digital Signature
        String encryptedprivhash = RSAUtils.encryptRSAWithPrivatekey(
                senderprivateKey,
                hashedMsg2Server);
        // 4) Append original message and encrypted hash
        String beforezipstring[] = { msg2server, encryptedprivhash };
        // 5) Apply zip to beforezipbytes[][]
        String afterzipstring[] = new String[beforezipstring.length];
        for (int i = 0; i < beforezipstring.length; i++) {
            afterzipstring[i] = CompressionUtils.compress(beforezipstring[i]);
            // log.info(afterzipstring[i]);
        }
        // 6) Encrypt zipstring with AES
        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        String afterzipstringAES[] = new String[afterzipstring.length + 1];
        for (int i = 0; i < afterzipstring.length; i++) {
            afterzipstringAES[i] = encryptAES(afterzipstring[i], key);
        }
        // 7) Encrypt AES key with Receiver Public Key using RSA
        String encodedKey = Base64
                .getEncoder()
                .encodeToString(key.getEncoded()); // SecretKey is base64 encoded since direct string encryption gives
                                                   // key
                                                   // in string format during decryption which cant be converted to
                                                   // SecretKey Format
        String keyencryptedwithreceiverpub = RSAUtils.encryptRSAWithPubkey(
                receiverpubKey,
                encodedKey);
        afterzipstringAES[2] = keyencryptedwithreceiverpub; // Decrypting AES key with Receiver Private Key using RSA
        String messagetoreceiver[] = afterzipstringAES;
        log.info(Arrays.toString(messagetoreceiver));
        log.info(senderpubKey.toString());
        log.info(senderprivateKey.toString());
        log.info(receiverpubKey.toString());
    }

    public void addLogMessage(String message) {
        log.info("Client | " + message);
        logMessages.add(message);
    }

    public ArrayList<String> getLogMessages() {
        return logMessages;
    }

    public static String[] senderside(
            String rawinput,
            PublicKey senderpubKey,
            PrivateKey senderprivateKey,
            PublicKey receiverpubKey) throws Exception {
        // Generating SHA-512 hash of original message
        String hashout = HashingUtils.sha512(rawinput);
        // Encrypt the message hash with sender private keys -> Digital Signature
        String encryptedprivhash = RSAUtils.encryptRSAWithPrivatekey(
                senderprivateKey,
                hashout);
        // Append original message and encrypted hash
        String beforezipstring[] = { rawinput, encryptedprivhash };
        // Apply zip to beforezipbytes[][]
        String afterzipstring[] = new String[beforezipstring.length];
        for (int i = 0; i < beforezipstring.length; i++) {
            afterzipstring[i] = CompressionUtils.compress(beforezipstring[i]);
            // log.info(afterzipstring[i]);
        }
        // Encrypt zipstring with AES
        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        String afterzipstringAES[] = new String[afterzipstring.length + 1];
        for (int i = 0; i < afterzipstring.length; i++) {
            afterzipstringAES[i] = encryptAES(afterzipstring[i], key);
            // log.info(afterzipstringAES[i]);
        }
        // Encrypt AES key with Receiver Public Key using RSA
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        // SecretKey is base64 encoded since direct string enccryption gives key in
        // string format during decryption which cant be converted to SecretKey Format
        String keyencryptedwithreceiverpub = RSAUtils.encryptRSAWithPubkey(
                receiverpubKey,
                encodedKey);
        // Decrypting AES key with Receiver Private Key using RSA
        afterzipstringAES[2] = keyencryptedwithreceiverpub;
        String messagetoreceiver[] = afterzipstringAES;
        for (int i = 0; i < messagetoreceiver.length; i++) {
            // log.info(messagetoreceiver[i]);
        }
        return messagetoreceiver;
    }

    public static String encryptAES(String str, SecretKey key) throws Exception {
        Cipher ecipher = Cipher.getInstance("AES");
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        // Encode the string into bytes using utf-8
        byte[] utf8 = str.getBytes("UTF8");
        // Encrypt
        byte[] enc = ecipher.doFinal(utf8);
        // Encode bytes to base64 to get a string
        return Base64.getEncoder().encodeToString(enc);
    }

    

    public static String encypytMessage2Server(String msg2server, PublicKey senderpubKey, PublicKey receiverpubKey,
            PrivateKey senderprivateKey) throws Exception {
        // 1) Writes to server; PGP hashing and encryption should occur here...
        // 2) Generating SHA-512 hash of original message
        String hashedMsg2Server = HashingUtils.sha512(msg2server);
        // 3) Encrypt the message hash with sender private keys -> Digital Signature
        String encryptedprivhash = RSAUtils.encryptRSAWithPrivatekey(
                senderprivateKey,
                hashedMsg2Server);
        // 4) Append original message and encrypted hash
        String beforezipstring[] = { msg2server, encryptedprivhash };
        // 5) Apply zip to beforezipbytes[][]
        String afterzipstring[] = new String[beforezipstring.length];
        for (int i = 0; i < beforezipstring.length; i++) {
            afterzipstring[i] = CompressionUtils.compress(beforezipstring[i]);
            // log.info(afterzipstring[i]);
        }
        // 6) Encrypt zipstring with AES
        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        String afterzipstringAES[] = new String[afterzipstring.length + 1];
        for (int i = 0; i < afterzipstring.length; i++) {
            afterzipstringAES[i] = encryptAES(afterzipstring[i], key);
        }
        // 7) Encrypt AES key with Receiver Public Key using RSA
        String encodedKey = Base64
                .getEncoder()
                .encodeToString(key.getEncoded()); // SecretKey is base64 encoded since direct string encryption gives
                                                   // key
                                                   // in string format during decryption which cant be converted to
                                                   // SecretKey Format
        String keyencryptedwithreceiverpub = RSAUtils.encryptRSAWithPubkey(
                receiverpubKey,
                encodedKey);
        afterzipstringAES[2] = keyencryptedwithreceiverpub; // Decrypting AES key with Receiver Private Key using RSA
        String messagetoreceiver[] = afterzipstringAES;
        String encryptedMessage = (Arrays.toString(messagetoreceiver));
        return encryptedMessage;
    }

}
