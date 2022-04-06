package com.alderaeney.mangaloidebackend.exceptions.handlers;

import com.alderaeney.mangaloidebackend.exceptions.FileTooBigException;
import com.alderaeney.mangaloidebackend.exceptions.OldPasswordDoesNotMatchException;
import com.alderaeney.mangaloidebackend.exceptions.PasswordsDoNotMatchException;
import com.alderaeney.mangaloidebackend.exceptions.UserByUsernameNotFound;
import com.alderaeney.mangaloidebackend.exceptions.UsernameTakenException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler(value = UserByUsernameNotFound.class)
    public ResponseEntity<Object> userByUsernameNotFoundException(UserByUsernameNotFound exception) {
        return new ResponseEntity<>("Player with username " + exception.username + " not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = UsernameTakenException.class)
    public ResponseEntity<Object> usernameTakenException(UsernameTakenException exception) {
        return new ResponseEntity<>("Username " + exception.username + " already taken", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = PasswordsDoNotMatchException.class)
    public ResponseEntity<Object> passwordsDoNotMatchException(PasswordsDoNotMatchException exception) {
        return new ResponseEntity<>(exception.msg, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = OldPasswordDoesNotMatchException.class)
    public ResponseEntity<Object> oldPasswordDoesNotMatchException(OldPasswordDoesNotMatchException exception) {
        return new ResponseEntity<>(exception.msg, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = FileTooBigException.class)
    public ResponseEntity<Object> fileTooBigException(FileTooBigException exception) {
        return new ResponseEntity<>(exception.msg, HttpStatus.NOT_ACCEPTABLE);
    }
}
