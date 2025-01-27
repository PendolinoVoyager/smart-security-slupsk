package com.kacper.iot_backend.notification;

public record NotificationRequest(
        String type,
        String message
) {
}
