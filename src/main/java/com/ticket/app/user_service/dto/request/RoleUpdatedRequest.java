package com.ticket.app.user_service.dto.request;

import com.ticket.app.user_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RoleUpdatedRequest {
    private Role role;
}
