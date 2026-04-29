package com.hireready.hireready.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OptimizeResponse {
    private Long id;
    private String optimizedText;
    private double matchScore;
    private LocalDateTime createdAt;
}
