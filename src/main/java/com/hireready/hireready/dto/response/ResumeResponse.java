package com.hireready.hireready.dto.response;
//just for when you upload or fetch a resume
//only confirms the save was succesfull

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ResumeResponse {

    private Long id;
    private String fileName;
    private boolean isMain;
    private LocalDateTime createdAt;}

