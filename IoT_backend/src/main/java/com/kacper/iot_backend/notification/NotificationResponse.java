package com.kacper.iot_backend.notification;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Integer id,
        String type,
        String message,
        Boolean has_seen,
        OffsetDateTime timestamp
) {
}
