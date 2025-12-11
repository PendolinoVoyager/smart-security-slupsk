package com.kacper.iot_backend.exception;

public class BadRequestYoloException extends RuntimeException {
    public BadRequestYoloException(String message) {
        super(message);
    }
}
