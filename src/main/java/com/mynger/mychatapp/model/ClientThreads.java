package com.mynger.mychatapp.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//this class is handles the threads responsible for communicating with clients
public class ClientThreads extends Thread {
    private Client c;
    private String client = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;
    private Scanner sc = new Scanner(System.in);
    private BufferedReader input = null;
    private PrintStream output = null;
    private Socket clientSocket = null;
    private ClientThreads[] threads = null;
    private final int users;
    String line =
      "\n----------------------------------------------------------------------------------------------------------------------\n";
    String line2 =
      "----------------------------------------------------------------------------------------------------------------------";
    String help =
      line +
      "To leave enter '/exit' in a new line.\nTo send private messages enter user name with '@' sign in front of name, a space and the message e.g. @Bob 'Hey Bob'\nTo display these intructions again enter '/help'" +
      line;
    private static final AES aes = new AES("some random stri");
    //private ArrayList<PubKeys> pubkeys = new ArrayList<>();
    private static Cipher ecipher, dcipher; //Required for AES
  
    public ClientThreads(Socket clientSocket, ClientThreads[] threads) {
      this.clientSocket = clientSocket;
      this.threads = threads;
      users = threads.length;
    }
  
    @Override
    public void run() {
      try {
        // input and output streams created for each client thread
        input =
          new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream())
          );
        output = new PrintStream(clientSocket.getOutputStream());
        ois = new ObjectInputStream(clientSocket.getInputStream());
  
        String user;
  
        while (true) {
          output.println("Enter your name to display in chat: ");
          //the first msg sent to the server is the username...
          user = input.readLine().trim();
  
          if (!user.contains("@")) {
            //c.setKeys(user); //generate and send server public and private keys...
            break;
          } else {
            log.info("The name should not contain '@' character.");
          }
        }
  
        //Opening messages for clients.
        output.println(
          line + "******* Welcome to ChatAPP, " + user + "! *******\n"
        );
        output.println(help);
        synchronized (this) {
          for (ClientThreads thread : threads) {
            if (thread != null && thread == this) {
              client = "@" + user;
              break;
            }
          }
          for (ClientThreads thread : threads) {
            if (thread != null && thread != this) {
              thread.output.println("***New user: " + user + " joined***");
            }
          }
        }
  
        //Handling communication between clients.
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  
        while (true) {
          //this is where the decryption of encrypted client messages occurs...
          String messagetoreceiver[] = (String[]) ois.readObject();
          PublicKey senderpubKey = (PublicKey) ois.readObject();
          PrivateKey senderprivateKey = (PrivateKey) ois.readObject();
          PublicKey receiverpubKey = (PublicKey) ois.readObject();
          PrivateKey receiverprivateKey = (PrivateKey) ois.readObject();
  
          String nxtMsg = receiverside(
            messagetoreceiver,
            senderpubKey,
            senderprivateKey,
            receiverpubKey,
            receiverprivateKey
          );
  
          Date date = new Date();
          // Privated messages directed to intended user
          if (nxtMsg.startsWith("@")) {
            String[] msg = nxtMsg.split("\\s", 2);
            if (msg.length > 1 && msg[1] != null) {
              msg[0] = msg[0].trim();
              msg[1] = msg[1].trim();
              if (!msg[1].isEmpty()) {
                synchronized (this) {
                  for (ClientThreads thread : threads) {
                    if (
                      thread != null &&
                      thread != this &&
                      thread.client != null &&
                      thread.client.equals(msg[0])
                    ) {
                      String msg2clients =
                        "<" +
                        dateFormat.format(date) +
                        "> " +
                        user +
                        ": " +
                        msg[1];
                      //this is where the encryption of msg[1] should occur...
  
                      thread.output.println(msg2clients); //output to client eg. @bob
                      this.output.println(msg2clients); //output to self
                      break;
                    }
                  }
                }
              }
            }
          } else if (nxtMsg.equals("/exit")) {
            break;
          } else if (nxtMsg.equals("/help")) {
            this.output.println(help);
          } else {
            // Public messages intended to all users
            synchronized (this) {
              for (ClientThreads thread : threads) {
                if (thread != null && thread.client != null) {
                  //this is where the encryption of nxtMsg should occur...
                  thread.output.println(
                    "<" + dateFormat.format(date) + "> " + user + ": " + nxtMsg
                  );
                }
              }
            }
          }
        }
  
        //when user exits, breaks out of loop and displays following message to all users
        synchronized (this) {
          for (ClientThreads thread : threads) {
            if (thread != null && thread != this && thread.client != null) {
              thread.output.println("***User: " + user + " left***");
            }
          }
        }
        output.println("***" + user + " You've logged out***");
  
        //Create space in threads list for another client
        synchronized (this) {
          for (int i = 0; i < threads.length; i++) {
            if (threads[i] == this) {
              threads[i] = null;
            }
          }
        }
        //Close socket, input and output streams.
        input.close();
        output.close();
        clientSocket.close();
      } catch (IOException e) {
        log.error(e.getMessage());
      } catch (Exception ex) {
        Logger
          .getLogger(ClientThreads.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  
    public static String receiverside(
      String messagetoreceiver[],
      PublicKey senderpubKey,
      PrivateKey senderprivateKey,
      PublicKey receiverpubKey,
      PrivateKey receiverprivateKey
    ) throws Exception {
      //Receiver receives the message messagetoreceiver[] with messagetoreceiver[2] as secret key encrypted with receiver pub key
      //Receiver decrypts the messagetoreceiver[2] with his/her privatekey
      String receiverencodedsecretkey = decrypt(
        receiverpubKey,
        receiverprivateKey,
        messagetoreceiver[2],
        1
      );
      //Key after decryption is in base64 encoded form
      byte[] decodedKey = Base64.getDecoder().decode(receiverencodedsecretkey);
      SecretKey originalKey = new SecretKeySpec(
        decodedKey,
        0,
        decodedKey.length,
        "AES"
      );
      //Decrypt the rest of the message in messagetoreceiver with SecretKey originalKey
      String receiverdecryptedmessage[] = new String[messagetoreceiver.length -
      1];
      for (int i = 0; i < messagetoreceiver.length - 1; i++) {
        messagetoreceiver[i] = decryptAES(messagetoreceiver[i], originalKey);
        //log.info(messagetoreceiver[i]);
      }
  
      //Unzip this message now i.e. unzip messagetoreceiver
      String unzipstring[] = new String[receiverdecryptedmessage.length];
      for (int i = 0; i < unzipstring.length; i++) {
        unzipstring[i] = decompress(messagetoreceiver[i]);
        //log.info(unzipstring[i]);
      }
  
      //Message has been received and is in unzipstring but check the digital signature of the sender i.e. verify the hash with senderpubkey
      //So decrypting the encrypted hash in unzipstring with sender pub key
      String receivedhash = decrypt(
        senderpubKey,
        senderprivateKey,
        unzipstring[1],
        0
      );
      //Calculating SHA512 at receiver side of message
      String calculatedhash = sha512(unzipstring[0]);
      if (receivedhash.equalsIgnoreCase(calculatedhash)) {}
  
      return unzipstring[0];
    }
  
    public static String encryptAES(String str, SecretKey key) throws Exception {
      ecipher = Cipher.getInstance("AES");
      ecipher.init(Cipher.ENCRYPT_MODE, key);
      // Encode the string into bytes using utf-8
      byte[] utf8 = str.getBytes("UTF8");
      // Encrypt
      byte[] enc = ecipher.doFinal(utf8);
      // Encode bytes to base64 to get a string
      return Base64.getEncoder().encodeToString(enc);
    }
  
    public static String decryptAES(String st, SecretKey key) throws Exception {
      dcipher = Cipher.getInstance("AES");
      dcipher.init(Cipher.DECRYPT_MODE, key);
      // Decode base64 to get bytes
      byte[] dec = Base64.getDecoder().decode(st);
      byte[] utf8 = dcipher.doFinal(dec);
      // Decode using utf-8
      return new String(utf8, "UTF8");
    }
  
    public static String decompress(String st) throws IOException {
      byte[] compressed = Base64.getDecoder().decode(st);
      ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
      GZIPInputStream gis = new GZIPInputStream(bis);
      BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      br.close();
      gis.close();
      bis.close();
      return sb.toString();
    }
  
    public static String compress(String data) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
      GZIPOutputStream gzip = new GZIPOutputStream(bos);
      gzip.write(data.getBytes());
      gzip.close();
      byte[] compressed = bos.toByteArray();
      bos.close();
      return Base64.getEncoder().encodeToString(compressed);
    }
  
    //Takes any string as input and calculates sha 512 bit hash. Output is in 128 bit hex string
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
  
    //n: 0->encryptwithprivatekey 1->encryptwithpublickey
    public static String encrypt(
      PublicKey publicKey,
      PrivateKey privateKey,
      String message,
      int ch
    ) throws Exception {
      Cipher cipher = Cipher.getInstance("RSA");
      if (ch == 0) {
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] utf8 = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(utf8);
      } else {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] utf8 = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(utf8);
      }
    }
  
    //n: 0->decryptwithpublickey 1->decryptwithprivatekey
    public static String decrypt(
      PublicKey publicKey,
      PrivateKey privateKey,
      String st,
      int ch
    ) throws Exception {
      Cipher cipher = Cipher.getInstance("RSA");
      byte[] encrypted = Base64.getDecoder().decode(st);
      if (ch == 0) {
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] utf8 = cipher.doFinal(encrypted);
        return new String(utf8, "UTF8");
      } else {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] utf8 = cipher.doFinal(encrypted);
        return new String(utf8, "UTF8");
      }
    }
  }
  