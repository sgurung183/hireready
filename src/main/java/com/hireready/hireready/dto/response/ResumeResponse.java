package com.hireready.hireready.dto.response;
//just for when you upload or fetch a resume
//only confirms the save was succesfull

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeResponse {

    private Long id;
    private String fileName;
    private String content;
    private boolean isMain;
    private LocalDateTime createdAt;
}

