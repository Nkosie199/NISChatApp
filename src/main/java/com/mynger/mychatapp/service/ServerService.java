package com.mynger.mychatapp.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mynger.mychatapp.model.Channel;
import com.mynger.mychatapp.model.Message;
import com.mynger.mychatapp.repository.MessageRepository;
import com.mynger.mychatapp.util.SenderUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ServerService {
  private static ArrayList<Channel> channels = new ArrayList<>();
  @Autowired
  private MessageRepository messageRepository;

  public Message addEncryptedMessage(Message message) throws Exception {
    String sender = message.getAuthor();
    String receiver = message.getRecipient();
    Channel channel = new Channel(sender, receiver);
    channels.add(channel);
    
    PrivateKey senderprivateKey = channel.getSender().getPrivateKey();
    PublicKey senderpubKey = channel.getSender().getPublicKey();
    PublicKey receiverpubKey = channel.getReciever().getPublicKey();
    
    String unencryptedMsg = message.getContent();
    String encryptedMsg = SenderUtils.encypytMessage2Server(unencryptedMsg, senderpubKey, receiverpubKey, senderprivateKey);
    
    message.setContent(encryptedMsg);
    return messageRepository.save(message);
  }

  public Message getDecryptedMessageById(Long messageId) throws BadRequestException {
    Message message = messageRepository.findById(messageId).orElseThrow(() -> new BadRequestException("Message not found"));
    return message;
  }
}
