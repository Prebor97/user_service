package com.ticket.app.user_service.dto.request;

import com.ticket.app.user_service.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

//@Data
//@AllArgsConstructor
//@RequiredArgsConstructor
public class RoleUpdatedRequest {
    @NotBlank(message = "Role name required")
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
