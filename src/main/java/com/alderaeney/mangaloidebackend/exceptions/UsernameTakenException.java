package com.alderaeney.mangaloidebackend.exceptions;

public class UsernameTakenException extends RuntimeException {
    public String username;

    public UsernameTakenException(String username) {
        this.username = username;
    }
}
