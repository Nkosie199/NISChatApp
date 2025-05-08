package com.mynger.mychatapp.service;

import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.mynger.mychatapp.model.Client;
import com.mynger.mychatapp.util.ReceiverUtils;
import com.mynger.mychatapp.util.SenderUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ServerService {
  private static ArrayList<String> messages = new ArrayList<>();
  private static ArrayList<Client> clients = new ArrayList<>();
  private static ObjectInputStream ois = null;
  String help = 
      "To leave enter '/exit' in a new line.\nTo send private messages enter user name with '@' sign in front of name, a space and the message e.g. @Bob 'Hey Bob'\nTo display these intructions again enter '/help'";

  public static void run() {
    try {
      // this is where the decryption of encrypted client messages occurs...
      String messagetoreceiver[] = (String[]) ois.readObject();
      PublicKey senderpubKey = (PublicKey) ois.readObject();
      PrivateKey senderprivateKey = (PrivateKey) ois.readObject();
      PublicKey receiverpubKey = (PublicKey) ois.readObject();
      PrivateKey receiverprivateKey = (PrivateKey) ois.readObject();

      String nxtMsg = ReceiverUtils.getDecryptedAESMessage(
          messagetoreceiver,
          senderpubKey,
          senderprivateKey,
          receiverpubKey,
          receiverprivateKey);

      // Handling communication between clients.
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      Date date = new Date();
      // Privatized messages directed to intended user
      if (nxtMsg.startsWith("@")) {
        String[] msg = nxtMsg.split("\\s", 2);
        if (msg.length > 1 && msg[1] != null) {
          String recipient = msg[0].trim();
          String pvtMsg = msg[1].trim();
          if (!pvtMsg.isEmpty()) {
            for (int i = 0; i < clients.size(); i++) {
              Client client = (Client) clients.get(i);
              if (client.getUsername().equals(recipient)){
                String msg2clients = "<" +
                    dateFormat.format(date) +
                    "> " +
                    recipient +
                    ": " +
                    pvtMsg;
                // this is where the encryption of msg[1] should occur...
                SenderUtils.sendMessageToServer(msg2clients); // output to client eg. @bob
                //sendMessageToSelf(msg2clients); // output to self
                break;
              }
            }
          }
        }
      } else if (nxtMsg.equals("/exit")) {
      } else if (nxtMsg.equals("/help")) {
        // this.output.println(help);
      } else {
        // Public messages intended to all users

        for (int i = 0; i < clients.size(); i++) {
          Client client = (Client) clients.get(i);
          if (client != null && client.getUsername() != null) {
          // this is where the encryption of nxtMsg should occur...
          SenderUtils.sendMessageToServer(
          "<" + dateFormat.format(date) + "> " + client.getUsername() + ": " + nxtMsg);
          }
        }
      }
    } catch (Exception ex) {
      log.error(ex.getMessage());
    }
  }

  public void addMessage(String message) {
    log.info("Server | " + message);
    messages.add(message);
  }

  public ArrayList<String> getMessages() {
    return messages;
  }
}
