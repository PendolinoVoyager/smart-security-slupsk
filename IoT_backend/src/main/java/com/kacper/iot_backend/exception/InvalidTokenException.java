package com.kacper.iot_backend.exception;

public class InvalidTokenException extends RuntimeException
{
    public InvalidTokenException(String msg) {
        super(msg);
    }
}
