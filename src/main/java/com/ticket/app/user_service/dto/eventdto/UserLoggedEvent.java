package com.ticket.app.user_service.dto.eventdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UserLoggedEvent {
    private String eventType = "UserLoggedIn";
    private String userId;
    private String email;
    private String name;
    @JsonProperty("login_date")
    private LocalDateTime loginDate;
}
