package com.hireready.hireready.dto.response;

import com.hireready.hireready.entity.VisaStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor //needed for the service layer can construct it, new AuthResponse
public class AuthResponse {
    private String token;
    private String fullName;
    private String email;
    private VisaStatus visaStatus;
}
