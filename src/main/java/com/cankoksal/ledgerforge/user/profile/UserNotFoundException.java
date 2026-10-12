package com.cankoksal.ledgerforge.user.profile;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}