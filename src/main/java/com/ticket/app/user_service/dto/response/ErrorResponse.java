package com.ticket.app.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;


public class ErrorResponse {
    private String error;
    private int code;
    private LocalDateTime timestamp;

    public ErrorResponse() {
    }

//    public ErrorResponse(String error, int code, LocalDateTime timestamp) {
//        this.error = error;
//        this.code = code;
//        this.timestamp = timestamp;
//    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
