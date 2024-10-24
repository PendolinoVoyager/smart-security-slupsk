package com.kacper.iot_backend.activation_token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Integer>
{
}
