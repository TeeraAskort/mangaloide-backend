package com.alderaeney.mangaloidebackend.exceptions;

public class ChapterNotFoundException extends RuntimeException {
    public Long number;

    public ChapterNotFoundException(Long number) {
        this.number = number;
    }
}
