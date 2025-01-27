package com.kacper.iot_backend.notification;

import com.kacper.iot_backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer>
{
    List<Notification> findByDevice_Id(Integer deviceId);

    @Query("SELECT n FROM Notification n WHERE n.device.user = :user ORDER BY n.timestamp DESC")
    Page<Notification> findByUser(User user, Pageable pageable);

}
