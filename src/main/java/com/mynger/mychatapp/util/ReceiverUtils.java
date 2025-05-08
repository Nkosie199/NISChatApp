package com.mynger.mychatapp.util;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiverUtils {
    public static String getDecryptedAESMessage(
      String messagetoreceiver[],
      PublicKey senderpubKey,
      PrivateKey senderprivateKey,
      PublicKey receiverpubKey,
      PrivateKey receiverprivateKey) throws Exception {
    // Receiver receives the message messagetoreceiver[] with messagetoreceiver[2]
    // as secret key encrypted with receiver pub key
    // Receiver decrypts the messagetoreceiver[2] with his/her privatekey
    String receiverencodedsecretkey = RSAUtils.decryptRSA(
        receiverpubKey,
        receiverprivateKey,
        messagetoreceiver[2],
        1);
    // Key after decryption is in base64 encoded form
    byte[] decodedKey = Base64.getDecoder().decode(receiverencodedsecretkey);
    SecretKey originalKey = new SecretKeySpec(
        decodedKey,
        0,
        decodedKey.length,
        "AES");
    // Decrypt the rest of the message in messagetoreceiver with SecretKey
    // originalKey
    String receiverdecryptedmessage[] = new String[messagetoreceiver.length -
        1];
    for (int i = 0; i < messagetoreceiver.length - 1; i++) {
      messagetoreceiver[i] = AESUtils.decryptAES(messagetoreceiver[i], originalKey);
      // log.info(messagetoreceiver[i]);
    }

    // Unzip this message now i.e. unzip messagetoreceiver
    String unzipstring[] = new String[receiverdecryptedmessage.length];
    for (int i = 0; i < unzipstring.length; i++) {
      unzipstring[i] = CompressionUtils.decompress(messagetoreceiver[i]);
      // log.info(unzipstring[i]);
    }

    // Message has been received and is in unzipstring but check the digital
    // signature of the sender i.e. verify the hash with senderpubkey
    // So decrypting the encrypted hash in unzipstring with sender pub key
    String receivedhash = RSAUtils.decryptRSA(
        senderpubKey,
        senderprivateKey,
        unzipstring[1],
        0);
    // Calculating SHA512 at receiver side of message
    String calculatedhash = HashingUtils.sha512(unzipstring[0]);
    if (receivedhash.equalsIgnoreCase(calculatedhash)) {
        log.info("Correct has received!");
    }

    return unzipstring[0];
  }


}
