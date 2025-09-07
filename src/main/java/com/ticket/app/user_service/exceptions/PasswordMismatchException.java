package com.ticket.app.user_service.exceptions;

public class PasswordMismatchException extends RuntimeException{

    public PasswordMismatchException(String message){
        super(message);
    }
}
