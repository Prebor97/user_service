package com.ticket.app.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
 public class UserResponse {
    private String jwt;
    private String message;
    private LocalDateTime timestamp;

     public UserResponse(String jwt, String message, LocalDateTime timestamp) {
         this.jwt = jwt;
         this.message = message;
         this.timestamp = timestamp;
     }
     public UserResponse(String message, LocalDateTime timestamp) {
         this.message = message;
         this.timestamp = timestamp;
     }


     public String getJwt() {
         return jwt;
     }

     public void setJwt(String jwt) {
         this.jwt = jwt;
     }

     public String getMessage() {
         return message;
     }

     public void setMessage(String message) {
         this.message = message;
     }

     public LocalDateTime getTimestamp() {
         return timestamp;
     }

     public void setTimestamp(LocalDateTime timestamp) {
         this.timestamp = timestamp;
     }
 }
