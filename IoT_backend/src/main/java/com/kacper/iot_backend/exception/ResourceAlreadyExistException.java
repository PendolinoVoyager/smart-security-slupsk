package com.kacper.iot_backend.exception;

public class ResourceAlreadyExistException extends RuntimeException
{
    public ResourceAlreadyExistException(String msg) {
        super(msg);
    }
}
