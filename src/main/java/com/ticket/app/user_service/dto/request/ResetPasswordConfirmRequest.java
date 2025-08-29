package com.ticket.app.user_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ResetPasswordConfirmRequest {
    private String token;
    private String password;
    @JsonProperty("confirm_password")
    private String confirmPassword;
}
