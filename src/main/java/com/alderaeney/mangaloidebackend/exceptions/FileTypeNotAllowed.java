package com.alderaeney.mangaloidebackend.exceptions;

public class FileTypeNotAllowed extends RuntimeException {
    public String type;

    public FileTypeNotAllowed(String type) {
        this.type = type;
    }
}
