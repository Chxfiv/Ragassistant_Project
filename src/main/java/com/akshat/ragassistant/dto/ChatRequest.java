package com.akshat.ragassistant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    private String question;

    private Long documentId;

}