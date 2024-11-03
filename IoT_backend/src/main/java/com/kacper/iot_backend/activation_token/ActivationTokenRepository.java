package com.kacper.iot_backend.activation_token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Integer>
{
    @Query(value = "DELETE FROM activation_tokens WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Integer userId);
}
