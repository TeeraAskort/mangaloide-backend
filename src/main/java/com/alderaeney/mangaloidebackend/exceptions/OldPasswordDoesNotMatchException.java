package com.alderaeney.mangaloidebackend.exceptions;

public class OldPasswordDoesNotMatchException extends RuntimeException {
    public String msg = "The old password provided does not match the stored one";
}
