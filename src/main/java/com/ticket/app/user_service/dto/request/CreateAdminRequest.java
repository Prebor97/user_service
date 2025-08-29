package com.ticket.app.user_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateAdminRequest {
private String email;
private String password;
private String firstName;
private String lastName;
}
