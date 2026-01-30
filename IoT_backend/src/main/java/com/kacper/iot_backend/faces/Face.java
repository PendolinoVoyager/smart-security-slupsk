package com.kacper.iot_backend.faces;

import jakarta.persistence.*;
import lombok.*;

import com.kacper.iot_backend.device.Device;

@Setter
@Getter
@Entity
@Table(name = "faces")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Face {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "face_name",
            nullable = false
    )
    private String faceName;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    // Maps one device to many faces
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

}
