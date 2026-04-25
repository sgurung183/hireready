package com.hireready.hireready.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OptimizeResponse {
    private int id;
    private String optimizedText;
    private double matchScore;
    private LocalDateTime createdAt;
}
