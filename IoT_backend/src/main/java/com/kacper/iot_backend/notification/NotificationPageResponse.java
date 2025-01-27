package com.kacper.iot_backend.notification;

import java.util.List;

public record NotificationPageResponse(
        Integer page,
        Integer total,
        List<NotificationResponse> notifications
) {
}
