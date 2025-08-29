package com.ticket.app.user_service.dto.eventdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class AdminCreatedEvent {
    private String eventType = "AdminCreated";
    private String userId;
    private String email;
    private String name;
}
