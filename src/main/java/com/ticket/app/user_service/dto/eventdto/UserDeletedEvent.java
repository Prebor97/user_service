package com.ticket.app.user_service.dto.eventdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

//@Data
//@AllArgsConstructor
//@RequiredArgsConstructor
public class UserDeletedEvent {
    private String eventType = "UserDeleted";
    private String userId;
    private String email;
    private String name;

    public UserDeletedEvent(String userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
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
}
