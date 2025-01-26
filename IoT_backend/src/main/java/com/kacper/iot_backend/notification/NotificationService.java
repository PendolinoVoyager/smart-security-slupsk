package com.kacper.iot_backend.notification;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService
{
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getNotifications(Integer id) {
        List<Notification> notifications = notificationRepository.findByDevice_Id(id);

        return notifications.stream()
                .map(notification -> new NotificationResponse(
                        notification.getType(),
                        notification.getMessage(),
                        notification.getHas_seen()
                ))
                .toList();
    }
}
