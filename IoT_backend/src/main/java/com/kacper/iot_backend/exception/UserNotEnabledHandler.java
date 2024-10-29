package com.kacper.iot_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserNotEnabledHandler
{
    @ExceptionHandler(UserNotEnabledException.class)
    public ResponseEntity<String> handleUserNotEnabledException(UserNotEnabledException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
