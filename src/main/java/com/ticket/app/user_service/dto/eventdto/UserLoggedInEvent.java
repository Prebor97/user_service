package com.ticket.app.user_service.dto.eventdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;



public class UserLoggedInEvent {
    private String eventType = "UserLoggedIn";
    private String userId;
    private String email;
    private String name;
    @JsonProperty("login_date")
    private LocalDateTime loginDate;

    public UserLoggedInEvent(String userId, String email, String name, LocalDateTime loginDate) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.loginDate = loginDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(LocalDateTime loginDate) {
        this.loginDate = loginDate;
    }
}
