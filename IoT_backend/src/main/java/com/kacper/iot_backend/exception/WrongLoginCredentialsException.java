package com.kacper.iot_backend.exception;

public class WrongLoginCredentialsException extends RuntimeException
{
    public WrongLoginCredentialsException(String msg) {
        super(msg);
    }
}
