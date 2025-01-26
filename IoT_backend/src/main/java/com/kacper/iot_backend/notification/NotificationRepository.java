package com.kacper.iot_backend.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer>
{
    List<Notification> findByDevice_Id(Integer deviceId);
}
