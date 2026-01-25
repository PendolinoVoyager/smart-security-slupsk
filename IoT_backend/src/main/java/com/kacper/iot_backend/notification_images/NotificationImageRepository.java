package com.kacper.iot_backend.notification_images;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationImageRepository extends JpaRepository<NotificationImage, Integer> {
    List<NotificationImage> findByNotificationId(Integer notificationId);
}
