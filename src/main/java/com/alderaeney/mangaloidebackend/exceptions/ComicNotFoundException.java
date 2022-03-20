package com.alderaeney.mangaloidebackend.exceptions;

public class ComicNotFoundException extends RuntimeException {
    public Long id;

    public ComicNotFoundException(Long id) {
        this.id = id;
    }
}
