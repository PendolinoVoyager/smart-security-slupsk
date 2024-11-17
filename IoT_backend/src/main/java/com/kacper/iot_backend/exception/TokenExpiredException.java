package com.kacper.iot_backend.exception;

public class TokenExpiredException extends RuntimeException
{
    public TokenExpiredException(String msg) {
        super(msg);
    }
}
