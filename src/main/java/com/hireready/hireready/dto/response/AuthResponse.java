package com.hireready.hireready.dto.response;

import com.hireready.hireready.entity.VisaStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String fullName;
    private String email;
    private VisaStatus visaStatus;
}
