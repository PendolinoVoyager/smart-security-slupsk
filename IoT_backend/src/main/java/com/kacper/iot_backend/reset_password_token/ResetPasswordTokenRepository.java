package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Integer>
{
    boolean existsByUser(User user);
}
