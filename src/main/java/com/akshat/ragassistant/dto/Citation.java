package com.akshat.ragassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Citation {

    private Long chunkId;

    private String fileName;

    private Integer chunkIndex;

}