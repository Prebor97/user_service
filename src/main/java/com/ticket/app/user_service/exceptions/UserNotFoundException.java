package com.ticket.app.user_service.exceptions;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String userId){
        super("User Not Found with ID: " + userId);
    }
}
