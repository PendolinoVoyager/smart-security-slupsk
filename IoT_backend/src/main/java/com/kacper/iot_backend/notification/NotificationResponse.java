package com.kacper.iot_backend.notification;

public record NotificationResponse(
        String type,
        String message,
        Boolean has_seen
) {
}
