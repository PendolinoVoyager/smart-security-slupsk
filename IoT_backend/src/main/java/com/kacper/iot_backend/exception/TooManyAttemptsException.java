package com.kacper.iot_backend.exception;

public class TooManyAttemptsException extends RuntimeException
{
    public TooManyAttemptsException(String msg) {
        super(msg);
    }
}
