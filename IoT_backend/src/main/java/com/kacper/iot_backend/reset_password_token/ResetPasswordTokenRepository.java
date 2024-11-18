package com.kacper.iot_backend.reset_password_token;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Integer>
{
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM reset_password_tokens WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(Integer userId);
}
