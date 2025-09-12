package com.ticket.app.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

//@Data
//@AllArgsConstructor
//@RequiredArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Mail required")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
