package com.alderaeney.mangaloidebackend.exceptions.handlers;

import com.alderaeney.mangaloidebackend.exceptions.CannotConvertImageException;
import com.alderaeney.mangaloidebackend.exceptions.CannotDeleteFolderException;
import com.alderaeney.mangaloidebackend.exceptions.CannotReadImageException;
import com.alderaeney.mangaloidebackend.exceptions.ChapterNotFoundException;
import com.alderaeney.mangaloidebackend.exceptions.ChapterNotUploaded;
import com.alderaeney.mangaloidebackend.exceptions.ComicNotFoundException;
import com.alderaeney.mangaloidebackend.exceptions.FileTypeNotAllowed;
import com.alderaeney.mangaloidebackend.exceptions.InvalidZipFile;

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

    @ExceptionHandler(value = ComicNotFoundException.class)
    public ResponseEntity<Object> comicNotFoundException(ComicNotFoundException exception) {
        return new ResponseEntity<>("Comic with id " + exception.id + " not found in the database",
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = InvalidZipFile.class)
    public ResponseEntity<Object> invalidZipFile(InvalidZipFile exception) {
        return new ResponseEntity<>(exception.msg, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = CannotConvertImageException.class)
    public ResponseEntity<Object> cannotConvertImageException(CannotConvertImageException exception) {
        return new ResponseEntity<>(exception.msg, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = CannotReadImageException.class)
    public ResponseEntity<Object> cannotReadImageException(CannotReadImageException exception) {
        return new ResponseEntity<>(exception.msg, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = CannotDeleteFolderException.class)
    public ResponseEntity<Object> cannotDeleteFolderException(CannotDeleteFolderException exception) {
        return new ResponseEntity<>(exception.msg, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = ChapterNotFoundException.class)
    public ResponseEntity<Object> chapterNotFoundException(ChapterNotFoundException exception) {
        return new ResponseEntity<>("Chapter number " + exception.number + " not found on the database",
                HttpStatus.NOT_FOUND);
    }
}
