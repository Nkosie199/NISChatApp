package com.mynger.mychatapp.service;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mynger.mychatapp.dto.MessageDTO;
import com.mynger.mychatapp.mapper.MessageMapper;
import com.mynger.mychatapp.model.Message;
import com.mynger.mychatapp.repository.MessageRepository;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ServerService serverService;

    public List<MessageDTO> getAll() {
        return messageRepository.findAll().stream()
                .map(message -> MessageMapper.toDTO(message))
                .toList();
    }

    public Message getByMessageId(Long id) throws BadRequestException {
        return messageRepository.findById(id).orElseThrow(() -> new BadRequestException("Message not found"));
    }

    public Message getByMessageAuthor(String author) throws BadRequestException {
        return messageRepository.findByAuthor(author).orElseThrow(() -> new BadRequestException("Message not found"));
    }

    public Message createMessage(MessageDTO messageDTO) throws Exception {
        Message message = MessageMapper.toDefaultEntity(messageDTO);
        if (message.getRecipient().isEmpty()) {
            return messageRepository.save(message);
        } else {
            return serverService.addEncryptedMessage(message);
        }
    }
}
