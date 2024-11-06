package com.kacper.iot_backend.activation_token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Integer>
{
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM activation_tokens WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Integer userId);
}
