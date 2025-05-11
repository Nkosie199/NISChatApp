package com.mynger.mychatapp.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import com.mynger.mychatapp.dto.MessageDTO;
import com.mynger.mychatapp.mapper.MessageMapper;
import com.mynger.mychatapp.model.Channel;
import com.mynger.mychatapp.model.Message;
import com.mynger.mychatapp.util.SenderUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ServerService {
  private static ArrayList<Channel> channels = new ArrayList<>();
  private MessageService messageService;

  public void addEncryptedMessage(Message message) throws Exception {
    String sender = message.getAuthor();
    String receiver = message.getRecipient();
    Channel channel = new Channel(sender, receiver);
    channels.add(channel);
    
    PrivateKey senderprivateKey = channel.getSender().getPrivateKey();
    PublicKey senderpubKey = channel.getSender().getPublicKey();
    PublicKey receiverpubKey = channel.getReciever().getPublicKey();
    
    String unencryptedMsg = message.getContent();
    String encryptedMsg = SenderUtils.encypytMessage2Server(unencryptedMsg, senderpubKey, receiverpubKey, senderprivateKey);
    
    MessageDTO messageDTO = MessageMapper.toDTO(message);
    messageDTO.setContent(encryptedMsg);
    messageService.createMessage(messageDTO);
  }

  public Message getDecryptedMessageById(Long messageId) throws BadRequestException {
    Message message = messageService.getByMessageId(messageId);
    return message;
  }
}
