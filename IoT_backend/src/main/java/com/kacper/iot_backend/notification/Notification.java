package com.kacper.iot_backend.notification;

import com.kacper.iot_backend.device.Device;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "notification_type",
            nullable = false
    )
    private String type;

    @Column(
            name = "message",
            nullable = false
    )
    private String message;

    @Column(
            name = "has_seen",
            nullable = false
    )
    private Boolean has_seen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
}
