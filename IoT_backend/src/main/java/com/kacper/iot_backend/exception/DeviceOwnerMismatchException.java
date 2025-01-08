package com.kacper.iot_backend.exception;


public class DeviceOwnerMismatchException extends RuntimeException
{
    public DeviceOwnerMismatchException(String msg) {
        super(msg);
    }
}
