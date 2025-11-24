package com.example.bank.exception;

import com.example.bank.model.User.User;

public class UserBlockedException extends RuntimeException {
    public UserBlockedException(User user) {
        super("Operation failed user: " + user.getUsername() + " is blocked");

    }
}
