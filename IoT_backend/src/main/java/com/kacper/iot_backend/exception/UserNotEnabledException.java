package com.kacper.iot_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserNotEnabledException extends RuntimeException
{
    public UserNotEnabledException(String msg) {
        super(msg);
    }
}
