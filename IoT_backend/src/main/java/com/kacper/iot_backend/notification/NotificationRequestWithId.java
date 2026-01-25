package com.kacper.iot_backend.notification;

public record NotificationRequestWithId(
        String type,
        String message,
        Integer deviceId
) {
}