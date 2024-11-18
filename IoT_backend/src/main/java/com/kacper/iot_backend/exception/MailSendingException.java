package com.kacper.iot_backend.exception;

public class MailSendingException extends RuntimeException {
    public MailSendingException(String message) {
        super(message);
    }
}
