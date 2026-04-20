package com.hireready.hireready.dto.request;

import com.hireready.hireready.entity.VisaStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private VisaStatus visaStatus;

    //id, role and createdAt are set by the server not the client
}
