package com.kacper.iot_backend.device;

import com.kacper.iot_backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "devices")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "address",
            nullable = false
    )
    private String address;

    @Column(
            name = "device_name",
            nullable = false
    )
    private String deviceName;

    @Column(
            name = "uuid",
            nullable = false
    )
    private String uuid;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;
}
