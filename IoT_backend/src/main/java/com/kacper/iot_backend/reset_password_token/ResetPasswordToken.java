package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "reset_password_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordToken
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "reset_password_token",
            nullable = false
    )
    private String token;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Date createdAt;

    @Column(
            name = "expired_at",
            nullable = false
    )
    private Date expiredAt;

    @Column(
            name = "attempts",
            nullable = false
    )
    private Integer attempts;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
