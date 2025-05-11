package com.mynger.mychatapp.mapper;

import com.mynger.mychatapp.dto.MessageDTO;
import com.mynger.mychatapp.model.Message;

public class MessageMapper {

    public static MessageDTO toDTO(Message message) {
        if (message == null) return null;

        return MessageDTO.builder()
                .id(message.getId())
                .title(message.getTitle())
                .content(message.getContent())
                .author(message.getAuthor())
                .recipient(message.getRecipient())
                .linkToContent(message.getLinkToContent())
                .timeOfUpload(message.getTimeOfUpload())
                .typeOfContent(message.getTypeOfContent())
                .sizeOfContent(message.getSizeOfContent())
                .underrated(message.getUnderrated())
                .rated(message.getRated())
                .overrated(message.getOverrated())
                .generalAudiences(message.getGeneralAudiences())
                .parentalGuidanceSuggested(message.getParentalGuidanceSuggested())
                .restricted(message.getRestricted())
                .build();
    }

    public static Message toEntity(MessageDTO dto) {
        if (dto == null) return null;

        Message message = new Message();
        message.setId(dto.getId());
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setAuthor(dto.getAuthor());
        message.setRecipient(dto.getRecipient());
        message.setLinkToContent(dto.getLinkToContent());
        message.setTimeOfUpload(dto.getTimeOfUpload());
        message.setTypeOfContent(dto.getTypeOfContent());
        message.setSizeOfContent(dto.getSizeOfContent());
        message.setUnderrated(dto.getUnderrated());
        message.setRated(dto.getRated());
        message.setOverrated(dto.getOverrated());
        message.setGeneralAudiences(dto.getGeneralAudiences());
        message.setParentalGuidanceSuggested(dto.getParentalGuidanceSuggested());
        message.setRestricted(dto.getRestricted());

        return message;
    }

    public static Message toDefaultEntity(MessageDTO dto) {
        if (dto == null) return null;

        Message message = new Message();
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setAuthor(dto.getAuthor());
        message.setRecipient(dto.getRecipient());
        message.setLinkToContent(dto.getLinkToContent());
        message.setTimeOfUpload(dto.getTimeOfUpload());
        message.setTypeOfContent(dto.getTypeOfContent());
        message.setSizeOfContent(dto.getSizeOfContent());
        message.setUnderrated(0);
        message.setRated(0);
        message.setOverrated(0);
        message.setGeneralAudiences(0);
        message.setParentalGuidanceSuggested(0);
        message.setRestricted(0);

        return message;
    }
}
