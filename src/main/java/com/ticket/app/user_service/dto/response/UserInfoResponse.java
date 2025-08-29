package com.ticket.app.user_service.dto.response;

import com.ticket.app.user_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UserInfoResponse {
    private String userId;
    private String email;
    private List<Role> role;
    private String address;
    private LocalDate dateOfBirth;
    private String profilePictureUrl;
}
