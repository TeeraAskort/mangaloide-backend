package com.alderaeney.mangaloidebackend.exceptions.handlers;

import com.alderaeney.mangaloidebackend.exceptions.ChapterNotUploaded;
import com.alderaeney.mangaloidebackend.exceptions.FileTypeNotAllowed;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ComicExceptionHandler {
    @ExceptionHandler(value = FileTypeNotAllowed.class)
    public ResponseEntity<Object> fileTypeNotAllowed(FileTypeNotAllowed exception) {
        return new ResponseEntity<>("File type " + exception.type + " not allowed", HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = ChapterNotUploaded.class)
    public ResponseEntity<Object> chapterNotUploaded(ChapterNotUploaded exception) {
        return new ResponseEntity<>(exception.msg, HttpStatus.NOT_FOUND);
    }
}
