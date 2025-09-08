package com.ticket.app.user_service.exceptions;

public class UserAccountNotActivatedException extends RuntimeException{
    public UserAccountNotActivatedException(String message){
        super(message);
    }
}
