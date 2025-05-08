package com.mynger.mychatapp.service;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mynger.mychatapp.dto.MessageDTO;
import com.mynger.mychatapp.model.Message;
import com.mynger.mychatapp.repository.MessageRepository;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public List<MessageDTO> getAll() {
        return messageRepository.findAll().stream()
                .map(message -> MessageDTO.builder()
                        .title(message.getTitle())
                        .content(message.getContent())
                        .author(message.getAuthor())
                        .linkToContent(message.getLinkToContent())
                        .timeOfUpload(message.getTimeOfUpload())
                        .typeOfContent(message.getTypeOfContent())
                        .sizeOfContent(message.getSizeOfContent())
                        .recipient(message.getRecipient())
                        .underrated(message.getUnderrated())
                        .rated(message.getRated())
                        .overrated(message.getOverrated())
                        .generalAudiences(message.getGeneralAudiences())
                        .parentalGuidanceSuggested(message.getParentalGuidanceSuggested())
                        .restricted(message.getRestricted())
                        .build())
                .toList();
    }

    public Message getByMessageAuthor(String author) throws BadRequestException {
        return messageRepository.findByAuthor(author).orElseThrow(() -> new BadRequestException("Message not found"));
    }

    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }
}
