package com.mynger.mychatapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(name = "link_to_content", nullable = false, length = 500)
    private String linkToContent;

    @Column(name = "time_of_upload", nullable = false)
    private Integer timeOfUpload;

    @Column(name = "type_of_content", nullable = false, length = 50)
    private String typeOfContent;

    @Column(name = "size_of_content", nullable = false)
    private Integer sizeOfContent;

    @Column(nullable = false, length = 100)
    private String recipient;

    @Column(nullable = false)
    private Integer underrated = 0;

    @Column(nullable = false)
    private Integer rated = 0;

    @Column(nullable = false)
    private Integer overrated = 0;

    @Column(name = "general_audiences", nullable = false)
    private Integer generalAudiences = 0;

    @Column(name = "parental_guidance_suggested", nullable = false)
    private Integer parentalGuidanceSuggested = 0;

    @Column(nullable = false)
    private Integer restricted = 0;
}
