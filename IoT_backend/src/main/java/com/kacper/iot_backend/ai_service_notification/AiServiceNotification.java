package com.kacper.iot_backend.ai_service_notification;

import com.kacper.iot_backend.notification_images.NotificationImage;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ai_service_notifications")
public class AiServiceNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "notification_type", nullable = false, length = 255)
    private String notificationType;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "has_seen", nullable = false)
    private boolean hasSeen;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

    @OneToMany(mappedBy = "aiServiceNotification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationImage> images = new ArrayList<>();
}
