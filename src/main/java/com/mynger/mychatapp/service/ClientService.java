package com.mynger.mychatapp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import com.mynger.mychatapp.model.Client;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClientService {

  static Socket MyClient = null; // uses TCP
  static Scanner s;
  private static ObjectOutputStream oos = null;
  private static BufferedReader serverInput = null;
  private static BufferedReader clientInput = null;
  private static PrintStream output = null;
  private static boolean socketClosed = false;
  public static String user;
  private static PublicKey senderpubKey;
  private static PrivateKey senderprivateKey;
  private static PublicKey receiverpubKey;
  private static PrivateKey receiverprivateKey;
  private static ArrayList<String> logMessages = new ArrayList<>();

  public void runClient(String host, int portNumber) {
    // setup connection...
    try {
      MyClient = new Socket(host, portNumber);
      clientInput = new BufferedReader(new InputStreamReader(System.in));
      output = new PrintStream(MyClient.getOutputStream());
      oos = new ObjectOutputStream(MyClient.getOutputStream());
      serverInput = new BufferedReader(new InputStreamReader(MyClient.getInputStream()));
    } catch (UnknownHostException e) {
      addLogMessage("This host is unknown: " + host);
    } catch (IOException e) {
      addLogMessage("Unable to get Input/output connection of host: " + host);
    }

    // main run loop contained here...
    if (MyClient != null && output != null && serverInput != null) {
      try {
        // Threads read server input
        new Thread(new Client()).start();
        // the first msg sent to the server is the username...
        user = clientInput.readLine().trim();
        Client.setKeys(user); // generate and send server public and private keys...
        output.println(user); // send msg to server...

        while (!socketClosed) {
          // 1) Writes to server; PGP hashing and encryption should occur here...
          String msg2server = clientInput.readLine().trim();
          // 2) Generating SHA-512 hash of original message
          String hashedMsg2Server = Client.sha512(msg2server);
          // 3) Encrypt the message hash with sender private keys -> Digital Signature
          String encryptedprivhash = Client.encrypt(
              senderpubKey,
              senderprivateKey,
              hashedMsg2Server,
              0);
          // 4) Append original message and encrypted hash
          String beforezipstring[] = { msg2server, encryptedprivhash };
          // 5) Apply zip to beforezipbytes[][]
          String afterzipstring[] = new String[beforezipstring.length];
          for (int i = 0; i < beforezipstring.length; i++) {
            afterzipstring[i] = Client.compress(beforezipstring[i]);
            // log.info(afterzipstring[i]);
          }
          // 6) Encrypt zipstring with AES
          SecretKey key = KeyGenerator.getInstance("AES").generateKey();
          String afterzipstringAES[] = new String[afterzipstring.length + 1];
          for (int i = 0; i < afterzipstring.length; i++) {
            afterzipstringAES[i] = Client.encryptAES(afterzipstring[i], key);
          }
          // 7) Encrypt AES key with Receiver Public Key using RSA
          String encodedKey = Base64
              .getEncoder()
              .encodeToString(key.getEncoded()); // SecretKey is base64 encoded since direct string encryption gives key
                                                 // in string format during decryption which cant be converted to
                                                 // SecretKey Format
          String keyencryptedwithreceiverpub = Client.encrypt(
              receiverpubKey,
              receiverprivateKey,
              encodedKey,
              1);
          afterzipstringAES[2] = keyencryptedwithreceiverpub; // Decrypting AES key with Receiver Private Key using RSA
          String messagetoreceiver[] = afterzipstringAES;
          oos.writeObject(messagetoreceiver);
          oos.writeObject(senderpubKey);
          oos.writeObject(senderprivateKey);
          oos.writeObject(receiverpubKey);
          oos.writeObject(receiverprivateKey);
        }
        output.close();
        serverInput.close();
        MyClient.close();
      } catch (Exception e) {
        addLogMessage("Exception: " + e);
      }
    }
  }

  public void addLogMessage(String message) {
    log.info("Client | " + message);
    logMessages.add(message);
  }

  public ArrayList<String> getLogMessages() {
    return logMessages;
  }

}
