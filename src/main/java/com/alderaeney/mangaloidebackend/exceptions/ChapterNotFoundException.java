package com.alderaeney.mangaloidebackend.exceptions;

public class ChapterNotFoundException extends RuntimeException {
    public Double number;

    public ChapterNotFoundException(Double number) {
        this.number = number;
    }
}
