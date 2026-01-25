package com.kacper.iot_backend.notification_images;

import com.kacper.iot_backend.notification.Notification;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_images")
public class NotificationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Override
    public String toString() {
        return "NotificationImage(id=" + this.getId() + ", imageUrl=" + this.getImageUrl() + ", notificationId=" + this.getNotification().getId() + ")";
    }
}
