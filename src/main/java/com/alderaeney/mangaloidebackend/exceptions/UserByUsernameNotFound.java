package com.alderaeney.mangaloidebackend.exceptions;

public class UserByUsernameNotFound extends RuntimeException {
    public String username;

    public UserByUsernameNotFound(String username) {
        this.username = username;
    }
}
