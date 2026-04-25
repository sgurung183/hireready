package com.hireready.hireready.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OptimizeRequest {
    @NotBlank
    private String jobDescriptionText;
}
