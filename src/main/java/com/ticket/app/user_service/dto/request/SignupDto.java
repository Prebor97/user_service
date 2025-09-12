package com.ticket.app.user_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@AllArgsConstructor
@RequiredArgsConstructor
public class SignupDto {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @JsonProperty("confirm_password")
    @NotBlank
    private String confirmPassword;
    @NotBlank(message = "FirstName is required")
    @Size(min = 3, max = 20, message = "FirstName must be between 3 and 20 characters")
    @JsonProperty("first_name")
    private String firstName;
    @NotBlank(message = "LastName is required")
    @Size(min = 3, max = 20, message = "LastName must be between 3 and 20 characters")
    @JsonProperty("last_name")
    private String lastName;
    @NotBlank(message = "Phone Number is required")
    @Size(min = 11, max = 15, message = "Phone Number must be between 11 and 15 characters")
    @JsonProperty("phone_number")
    private String phoneNumber;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
