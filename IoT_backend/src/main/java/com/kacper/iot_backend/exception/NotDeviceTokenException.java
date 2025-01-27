package com.kacper.iot_backend.exception;

public class NotDeviceTokenException extends RuntimeException
{
    public NotDeviceTokenException(String msg) {
        super(msg);
    }
}
