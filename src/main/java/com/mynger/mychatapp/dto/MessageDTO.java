package com.mynger.mychatapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDTO {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String linkToContent;
    private Integer timeOfUpload;
    private String typeOfContent;
    private Integer sizeOfContent;
    private String recipient;

    private Integer underrated;
    private Integer rated;
    private Integer overrated;

    private Integer generalAudiences;
    private Integer parentalGuidanceSuggested;
    private Integer restricted;
}
